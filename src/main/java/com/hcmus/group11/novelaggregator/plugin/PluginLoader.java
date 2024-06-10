package com.hcmus.group11.novelaggregator.plugin;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
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
    private String packageName;
    private final Path pluginsDir;
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    private final Map<String, Runnable> tasks = new ConcurrentHashMap<>();
    private Timer processDelayTimer = null;
    private static final long DELAY = 2000; // milliseconds

    public PluginLoader(String packageName, Class<T> TClass, ConfigurableApplicationContext applicationContext) throws IOException {
        this.packageName = packageName;
        this.TClass = TClass;
        this.applicationContext = applicationContext;
        pluginsDir = Paths.get("src/main/java/" + packageName.replace(".", "/"));

        clearPlugindirectory();
        loadExistingPlugins();
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
                    addTask(className, () -> loadT(file.toFile()));
                }
            });
            runAllTasks(); // Run tasks immediately to load existing plugins
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading existing plugins", e);
        }
    }

    private void watchPluginsDirectory() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        pluginsDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

        Thread watchThread = new Thread(() -> {
            try {
                WatchKey key;
                while ((key = watchService.take()) != null) {
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
    }

    private void handleWatchEvent(WatchEvent<?> event) {
        Path pluginPath = pluginsDir.resolve((Path) event.context());
        // Skip .class files
        if (pluginPath.toString().endsWith(".class")) {
            return;
        }

        File pluginFile = pluginPath.toFile();
        String className = extractClassName(pluginFile);

        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            addTask(className, () -> loadT(pluginFile));
        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            addTask(className, () -> unloadT(pluginFile));
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
        }, DELAY);
    }

    private void runAllTasks() {
        tasks.values().forEach(Runnable::run);
    }

    public void loadT(File file) {
        try {
            File javaFile = new File(file.getPath().replace(".java~", ".java"));
            int compileResult = compiler.run(null, null, null, javaFile.getPath(), "-d", "src/main/java", "-Xlint:-options");

            if (compileResult == 0) {
                File compiledFile = new File(file.getPath().replace(".java", ".class"));
                URL[] urls = new URL[]{compiledFile.toURI().toURL()};
                try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
                    String className = extractClassName(javaFile);
                    Class<?> clazz = classLoader.loadClass(packageName + "." + className);

                    if (TClass.isAssignableFrom(clazz)) {
                        unregisterBean(className);
                        registerBean(className, clazz);
                        LOGGER.log(Level.INFO, "Loaded plugin: " + className);
                    } else {
                        LOGGER.log(Level.SEVERE, "Class does not implement the correct interface: " + javaFile.getName());
                    }
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
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    public void unloadT(File file) {
        String className = extractClassName(file);
        unregisterBean(className);
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

    public Map<String, T> getPlugins() {
        return applicationContext.getBeansOfType(TClass);
    }
}