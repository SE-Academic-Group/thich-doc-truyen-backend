package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.type.PluginMetadata;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PluginManager {
    private Map<String, INovelPlugin> novelPluginMap;
    private PluginLoader pluginLoader;

    public PluginManager(Map<String, INovelPlugin> novelPluginMap, PluginLoader pluginLoader) {
        this.novelPluginMap = novelPluginMap;
        this.pluginLoader = pluginLoader;
    }

    public INovelPlugin getPlugin(String pluginName) {
        INovelPlugin plugin = novelPluginMap.get(pluginName);
        if (plugin == null) {
            // Get the first plugin if pluginName is null
            plugin = novelPluginMap.values().iterator().next();
        }

        return plugin;
    }

    public List<PluginMetadata> getPluginMetadataList() {
        List<PluginMetadata> pluginMetadataList = new ArrayList<>();
        for (INovelPlugin plugin : novelPluginMap.values()) {
            PluginMetadata pluginMetadata = new PluginMetadata();
            pluginMetadata.setName(plugin.getPluginName());
            pluginMetadata.setUrl(plugin.getPluginUrl());
            pluginMetadataList.add(pluginMetadata);
        }

        return pluginMetadataList;
    }
}
