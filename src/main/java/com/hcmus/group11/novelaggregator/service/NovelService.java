package com.hcmus.group11.novelaggregator.service;

import com.hcmus.group11.novelaggregator.plugin.novel.INovelPlugin;
import com.hcmus.group11.novelaggregator.plugin.novel.PluginManager;
import com.hcmus.group11.novelaggregator.type.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    public List<SwitchPluginMetaData> getSwitchPluginMetaData(String chapterIndex, String novelUrl) {
        List<SwitchPluginMetaData> switchPluginMetaDataList = new ArrayList<>();

        INovelPlugin currentPlugin = pluginManager.getPluginByNovelUrl(novelUrl);
        NovelDetail novelDetail = currentPlugin.getNovelDetail(novelUrl);
        String title = currentPlugin.normalizeString(novelDetail.getTitle(), true);
        String author = currentPlugin.normalizeString(novelDetail.getAuthor(), false);

        List<PluginMetadata> pluginMetadataList = pluginManager.getPluginMetadataList();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<CompletableFuture<SwitchPluginMetaData>> futures = pluginMetadataList.stream()
                .filter(pluginMetadata -> !pluginMetadata.getName().equals(currentPlugin.getPluginName()))
                .map(pluginMetadata -> CompletableFuture.supplyAsync(() -> {
                    SwitchPluginMetaData switchPluginMetaData = new SwitchPluginMetaData();

                    INovelPlugin plugin = pluginManager.getPlugin(pluginMetadata.getName());
                    switchPluginMetaData.setPluginMetadata(pluginMetadata);
                    NovelSearchResult novelSearchResult = plugin.findSimilarNovel(title, author);
                    ChapterInfo chapterInfo = null;

                    if (novelSearchResult != null) {
                        chapterInfo = plugin.getChapterInfoByNovelUrlAndChapterIndex(novelSearchResult.getUrl(), chapterIndex);
                    }

                    switchPluginMetaData.setNovelSearchResult(novelSearchResult);
                    switchPluginMetaData.setChapterInfo(chapterInfo);

                    return switchPluginMetaData;
                }, executorService))
                .collect(Collectors.toList());

        // Chờ tất cả các CompletableFuture hoàn thành và thu thập kết quả
        for (CompletableFuture<SwitchPluginMetaData> future : futures) {
            try {
                switchPluginMetaDataList.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        // Tắt ExecutorService khi đã hoàn thành
        executorService.shutdown();

        return switchPluginMetaDataList;
    }

    public Object convertHtmlToEpub(String url) throws IOException {
        INovelPlugin plugin = pluginManager.getPluginByNovelUrl(url);
        return plugin.convertHtmlToEpub(url);
    }

    public Object convertHtmlToPdf(String url) throws IOException {
        INovelPlugin plugin = pluginManager.getPluginByNovelUrl(url);
        return plugin.convertHtmlToPdf(url);
    }

    public Object convertHtmlToImg(String url) throws Exception {
        INovelPlugin plugin = pluginManager.getPluginByNovelUrl(url);
        return plugin.convertHtmlToImg(url);
    }

    public List<DownloadOptions> getDownloadOptionsList() {
        return pluginManager.getDownloadOptionsList();
    }

    public Object exportNovel(String url, String type) throws Exception {
        INovelPlugin plugin = pluginManager.getPluginByNovelUrl(url);
        switch (type) {
            case "epub":
                return plugin.convertHtmlToEpub(url);
            case "pdf":
                return plugin.convertHtmlToPdf(url);
            case "img":
                return plugin.convertHtmlToImg(url);
            default:
                throw new RuntimeException("Invalid type");
        }
    }
}
