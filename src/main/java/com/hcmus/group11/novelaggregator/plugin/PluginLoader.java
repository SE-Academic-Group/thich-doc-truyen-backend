package com.hcmus.group11.novelaggregator.plugin;

import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class PluginLoader {

    private final Map<String, INovelPlugin> loadedINovelPlugins = new HashMap<>();
    private final Path pluginsDir = Path.of("src/main/java/com/hcmus/group11/novelaggregator/plugin/plugins");
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public PluginLoader() throws IOException {
        watchINovelPluginsDirectory();
    }

    private void watchINovelPluginsDirectory() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        pluginsDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

        Thread watchThread = new Thread(() -> {
            try {
                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path pluginPath = pluginsDir.resolve((Path) event.context());
                            loadINovelPlugin(pluginPath.toFile());
                        } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            Path pluginPath = pluginsDir.resolve((Path) event.context());
                            unloadINovelPlugin(pluginPath.toFile());
                        }
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        watchThread.start();
    }

    public void loadINovelPlugin(File file) {
        try {

            file = new File(file.getPath().replace(".java~", ".java"));
            compiler.run(null, null, null, file.getPath());
            // Make sure file point to the compiled class file
            file = new File(file.getPath().replace(".java", ".class"));
            URL[] urls = {file.toURI().toURL()};


            URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
            String className = extractClassName(file);
            Class<?> clazz = classLoader.loadClass(className);
            if (INovelPlugin.class.isAssignableFrom(clazz)) {
                INovelPlugin plugin = (INovelPlugin) clazz.getDeclaredConstructor().newInstance();
                loadedINovelPlugins.put(file.getName(), plugin);
                System.out.println("Loaded plugin: " + file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unloadINovelPlugin(File file) {
        INovelPlugin plugin = loadedINovelPlugins.remove(file.getName());
        if (plugin != null) {
            System.out.println("Unloaded plugin: " + file.getName());
        }
    }

    private String extractClassName(File file) {
        // Assuming the plugin class name is the same as the file name without the ".class" extension
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public Map<String, INovelPlugin> getLoadedINovelPlugins() {
        return loadedINovelPlugins;
    }
}

