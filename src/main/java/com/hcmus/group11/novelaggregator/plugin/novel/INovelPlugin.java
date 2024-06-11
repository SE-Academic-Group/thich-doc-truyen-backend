package com.hcmus.group11.novelaggregator.plugin.novel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hcmus.group11.novelaggregator.type.*;

import java.util.List;

public interface INovelPlugin {
    String getPluginName();

    String getPluginUrl();

    List<NovelSearchResult> search(String keyword, Integer page);

    NovelDetail getNovelDetail(String url);

    List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page);

    ChapterDetail getChapterDetail(String url);

    List<ChapterInfo> getFullChapterList(String url);

    String normalizeString(String str, Boolean isSpace);

    NovelSearchResult findSimilarNovel(String title, String author);

    ChapterInfo getChapterInfoByNovelUrlAndChapterIndex(String novelUrl, String chapterIndex);

    NovelDownloadInfo getNovelDownloadInfo(String url) throws JsonProcessingException;
}