package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.type.PluginMetadata;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
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
//            // Get the first plugin if pluginName is null
//            plugin = novelPluginMap.values().iterator().next();
            throw HttpException.NOT_FOUND("NOT_FOUND", "Plugin not found: " + pluginName);
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

    public INovelPlugin getPluginByNovelUrl(String url){
        try {
            URL detailUrl = new URL(url);
            String baseUrl = detailUrl.getProtocol() + "://" + detailUrl.getHost();
            baseUrl = baseUrl.toLowerCase();

            for (INovelPlugin plugin : novelPluginMap.values()) {
                String pluginName = plugin.getPluginName().toLowerCase();
                if (baseUrl.contains(pluginName)) {
                    return plugin;
                }
            }

            throw HttpException.NOT_FOUND("NOT_FOUND", "Plugin not found for url: " + url);

        }catch (MalformedURLException e){
            throw new RuntimeException(e.getMessage());
        }

    }
}
