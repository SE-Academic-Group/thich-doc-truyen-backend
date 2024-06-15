package com.hcmus.group11.novelaggregator.config;

import com.hcmus.group11.novelaggregator.plugin.PluginLoader;
import com.hcmus.group11.novelaggregator.plugin.download.IDownloadPlugin;
import com.hcmus.group11.novelaggregator.plugin.novel.INovelPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class PluginLoaderConfig {
    @Value("${plugins.package.name.novel-plugins}")
    private String novelPluginsPackageName;

    @Value("${plugins.package.name.download-plugins}")
    private String downloadPluginsPackageName;

    @Bean
    public PluginLoader<INovelPlugin> pluginLoader(ConfigurableApplicationContext applicationContext) throws IOException {
        Class<INovelPlugin> clazz = INovelPlugin.class;
        return new PluginLoader<>(novelPluginsPackageName, clazz, applicationContext);
    }

    @Bean
    public PluginLoader<IDownloadPlugin> downloadPluginLoader(ConfigurableApplicationContext applicationContext) throws IOException {
        Class<IDownloadPlugin> clazz = IDownloadPlugin.class;
        return new PluginLoader<>(downloadPluginsPackageName, clazz, applicationContext);
    }
}

