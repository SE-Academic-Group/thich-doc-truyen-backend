package com.hcmus.group11.novelaggregator.service;

import com.hcmus.group11.novelaggregator.plugin.INovelPlugin;
import com.hcmus.group11.novelaggregator.plugin.PluginManager;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.PluginMetadata;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NovelService {
    private PluginManager pluginManager;

    NovelService(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public List<NovelSearchResult> search(String keyword, Integer page, String pluginName) {
        INovelPlugin plugin = pluginManager.getPlugin(pluginName);
        return plugin.search(keyword, page);
    }

    public NovelDetail getNovelDetail(String url, String pluginName) {
        INovelPlugin plugin = pluginManager.getPlugin(pluginName);
        return plugin.getNovelDetail(url);
    }

    public List<PluginMetadata> getPluginList() {
        return pluginManager.getPluginMetadataList();
    }
}
