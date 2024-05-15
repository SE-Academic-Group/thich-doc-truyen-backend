package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.type.NovelSearchResult;

import java.util.List;

public interface INovelPlugin {
    String getPluginName();
    String getPluginUrl();
    List<NovelSearchResult> search(String keyword);
//    NovelDetail searchNovelDetail(String novelTitle);
//    NovelDetail getNovelDetail(NovelSearchResult novelSearchResult);
//    ChapterDetail getChapterDetail(NovelDetail novelDetail, int chapterIndex);
}