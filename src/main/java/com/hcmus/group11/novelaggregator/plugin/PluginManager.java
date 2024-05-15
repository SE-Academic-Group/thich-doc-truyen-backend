package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.plugin.INovelPlugin;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.PluginMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PluginManager {
    private Map<String, INovelPlugin> novelPluginMap;
    public PluginManager(Map<String, INovelPlugin> novelPluginMap) {
        this.novelPluginMap = novelPluginMap;
    }

    public INovelPlugin getPlugin(String pluginName) {
        return novelPluginMap.get(pluginName);
    }

    public List<PluginMetadata> getPluginMetadataList() {
        List<PluginMetadata> pluginMetadataList = new ArrayList<>();
        for (INovelPlugin plugin: novelPluginMap.values()) {
            PluginMetadata pluginMetadata = new PluginMetadata();
            pluginMetadata.setPluginName(plugin.getPluginName());
            pluginMetadata.setPluginUrl(plugin.getPluginUrl());
            pluginMetadataList.add(pluginMetadata);
        }

        return pluginMetadataList;
    }
}
