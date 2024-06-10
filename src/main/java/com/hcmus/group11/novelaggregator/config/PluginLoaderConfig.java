package com.hcmus.group11.novelaggregator.config;

import com.hcmus.group11.novelaggregator.plugin.INovelPlugin;
import com.hcmus.group11.novelaggregator.type.PluginLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class PluginLoaderConfig {
    @Value("${plugins.package.name.novel-plugins}")
    private String novelPluginsPackageName;

    @Bean
    public PluginLoader<INovelPlugin> pluginLoader() throws IOException {
        return new PluginLoader<>(novelPluginsPackageName);
    }
}

