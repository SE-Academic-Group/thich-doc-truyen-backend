package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.util.CustomClassLoader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class PluginLoader<T> {
    private static final Logger LOGGER = Logger.getLogger(PluginLoader.class.getName());

    private final Class<T> TClass;
    private final ConfigurableApplicationContext applicationContext;
    private final String packageName;
    private final Path pluginsDir;
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    private final Map<String, Runnable> tasks = new ConcurrentHashMap<>();

    private Timer processDelayTimer = null;
    private static final long DELAY = 1000; // milliseconds

    public PluginLoader(String packageName, Class<T> TClass, ConfigurableApplicationContext applicationContext) throws IOException {
        this.packageName = packageName;
        this.TClass = TClass;
        this.applicationContext = applicationContext;
        pluginsDir = Paths.get("src/main/java/" + packageName.replace(".", "/"));

        clearPlugindirectory();
//        loadExistingPlugins();
        watchPluginsDirectory();
    }

    private void clearPlugindirectory() {
        try {
            Files.list(pluginsDir).forEach(file -> {
                if (file.toString().endsWith(".class")) {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error deleting file: " + file, e);
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error clearing plugin directory", e);
        }
    }

    private void loadExistingPlugins() {
        try {
            Files.list(pluginsDir).forEach(file -> {
                String className = extractClassName(file.toFile());
                if (file.toString().endsWith(".java")) {
                    addTask(className, () -> loadT(className));
                }
            });
            runAllTasks(); // Run tasks immediately to load existing plugins
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading existing plugins", e);
        }
    }

    private void watchPluginsDirectory() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            pluginsDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            Thread watchThread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take(); // this will return the keys
                        for (WatchEvent<?> event : key.pollEvents()) {
                            handleWatchEvent(event);
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Watch thread interrupted", e);
                }
            });
            watchThread.start();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error watching plugin directory", e);
        }
    }

    private void handleWatchEvent(WatchEvent<?> event) {
        Path pluginPath = pluginsDir.resolve((Path) event.context());
        // Skip .class files
        if (!pluginPath.toString().endsWith(".java")) {
            return; // Ignore non-Java files and temporary files
        }
        String className = pluginPath.toString().substring(pluginsDir.toString().length() + 1).replace(".java", "");

        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            addTask(className, () -> loadT(className));
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            addTask(className, () -> unloadT(pluginPath.toFile()));
        }

    }

    private void addTask(String className, Runnable task) {
        tasks.put(className.toLowerCase(), task);

        if (processDelayTimer != null) {
            processDelayTimer.cancel();
        }
        processDelayTimer = new Timer();
        processDelayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runAllTasks();
                tasks.clear();
            }
        }, PluginLoader.DELAY);
    }

    private void runAllTasks() {
        tasks.values().forEach(Runnable::run);
    }

    public void loadT(String className) {
        try {
            // Ensure file content is fully written before compiling (Important!)
            File javaFile = new File(pluginsDir + "/" + className + ".java");
            while (javaFile.length() == 0 || !javaFile.canRead()) {
                try {
                    Thread.sleep(100); // Brief pause to allow for file writing
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Thread interrupted while waiting for file: " + javaFile.getName(), e);
                }
            }

            // Remove old .class file if it exists
            File oldClassFile = new File(javaFile.getPath().replace(".java", ".class").replace("~", ""));
            if (oldClassFile.exists()) {
                oldClassFile.delete();
            }

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int compileResult = compiler.run(null, null, null, javaFile.getPath(), "-d", "src/main/java", "-Xlint:-options");

            if (compileResult == 0) {
                File compiledFile = new File(javaFile.getAbsolutePath().replace(".java", ".class"));
                URL[] urls = new URL[]{compiledFile.toURI().toURL()};
                // Sleep for a while to allow the file to be fully written
                Thread.sleep(DELAY * 2);

                Class<T> clazz = loadClass(compiledFile.getAbsolutePath());
                if (clazz != null) {
                    String beanName = getBeanName(className);
                    unregisterBean(beanName);
                    registerBean(beanName, clazz);
                    LOGGER.log(Level.INFO, "Loaded plugin: " + className);
                }

            } else {
                LOGGER.log(Level.SEVERE, "Failed to compile plugin: " + javaFile.getName());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading plugin", e);
        }
    }

    private void registerBean(String beanName, Class<?> beanClass) {
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        BeanDefinition beanDefinition = builder.getRawBeanDefinition();

        if (beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.removeBeanDefinition(beanName);
        }
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    public void unloadT(File file) {
        String className = extractClassName(file);
        String beanName = getBeanName(className);
        unregisterBean(beanName);
        LOGGER.log(Level.INFO, "Unloaded plugin: " + className);
    }

    private void unregisterBean(String beanName) {
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        if (beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.removeBeanDefinition(beanName);
        }
    }

    private String extractClassName(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private String getBeanName(String className) {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    public Map<String, T> getPlugins() {
        return applicationContext.getBeansOfType(TClass);
    }

    Class<T> loadClass(String pathToClassFile) {
        //Declare the process builder to execute class file at run time (Provided filepath to class)
        ProcessBuilder pb = new ProcessBuilder("javap", pathToClassFile);
        try {
            //Start the process builder
            Process p = pb.start();

            //Declare string to hold class name
            String classname = null;
            //Declare buffer reader to read the class file & get class name
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (null != (line = br.readLine())) {
                if (line.startsWith("public class")) {
                    classname = line.split(" ")[2];
                    break;
                }
            }

            byte[] classData = Files.readAllBytes(Paths.get(pathToClassFile));
            CustomClassLoader classLoader = new CustomClassLoader(classData);
            Class<?> clazz = classLoader.findClass(classname);

            return (Class<T>) clazz;
        } catch (Exception e) {
            if (e instanceof ClassNotFoundException) {
                System.out.println("Class not found: " + e.getMessage());
            } else {
                System.out.println("Error: " + e.getMessage());
            }
            e.printStackTrace();
            return null;
        }
    }
}