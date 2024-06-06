package com.hcmus.group11.novelaggregator.service;

import com.hcmus.group11.novelaggregator.plugin.INovelPlugin;
import com.hcmus.group11.novelaggregator.plugin.PluginManager;
import com.hcmus.group11.novelaggregator.type.*;
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

    public NovelDetail getNovelDetail(String url) {
        INovelPlugin plugin = pluginManager.getPluginByNovelUrl(url);
        return plugin.getNovelDetail(url);
    }

    public List<ChapterInfo> getChapterList(String url, Integer page) {
        INovelPlugin plugin = pluginManager.getPluginByNovelUrl(url);
        return plugin.getChapterList(url, page);
    }

    public List<PluginMetadata> getPluginList() {
        return pluginManager.getPluginMetadataList();
    }

    public ChapterDetail getChapterDetail(String url) {
        INovelPlugin plugin = pluginManager.getPluginByNovelUrl(url);
        return plugin.getChapterDetail(url);
    }

    public List<ChapterInfo> getFullChapterList(String url) {
        INovelPlugin plugin = pluginManager.getPluginByNovelUrl(url);
        return plugin.getFullChapterList(url);
    }
}
