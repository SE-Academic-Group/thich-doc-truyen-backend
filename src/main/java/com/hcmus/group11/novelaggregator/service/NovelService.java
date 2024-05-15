package com.hcmus.group11.novelaggregator.service;

import com.hcmus.group11.novelaggregator.plugin.INovelPlugin;
import com.hcmus.group11.novelaggregator.plugin.PluginManager;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.PluginMetadata;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class NovelService {
    private PluginManager pluginManager;

    NovelService(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public List<NovelSearchResult> search(String keyword, String pluginName) {
        INovelPlugin plugin = pluginManager.getPlugin(pluginName);
        return plugin.search(keyword);
    }

    public List<PluginMetadata> getPluginList() {
        return pluginManager.getPluginMetadataList();
    }
}
