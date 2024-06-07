package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.type.ChapterDetail;
import com.hcmus.group11.novelaggregator.type.ChapterInfo;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;

import java.io.IOException;
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

    Object convertHtmlToPdf(String url) throws IOException;

    Object convertHtmlToEpub(String url) throws IOException;

    Object convertHtmlToImg(String url) throws Exception;

}