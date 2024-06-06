package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.type.ChapterDetail;
import com.hcmus.group11.novelaggregator.type.ChapterInfo;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseApi implements INovelPlugin {
    protected String pluginName;
    protected String pluginUrl;

    public String getPluginName() {
        return pluginName;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }

    @Override
    public ChapterDetail getChapterDetail(String url) {
        String jsonChapterDetailString = getJsonString(url);

        ChapterDetail chapterDetail = getChapterDetailFromJsonString(jsonChapterDetailString);
        if (chapterDetail == null) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for chapter url: " + url);
        }
        return chapterDetail;
    }

    @Override
    public List<ChapterInfo> getChapterList(String url, Integer page) {
        String chapterListUrl = buildChapterListUrlFromNovelDetailUrl(url, page);
        String jsonChapterListString = getJsonString(chapterListUrl);

        List<ChapterInfo> chapterList = getChapterListFromJsonString(jsonChapterListString);
        if (chapterList == null || chapterList.isEmpty()) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for novel url: " + url + " page: " + page);
        }

        return chapterList;
    }

    @Override
    public NovelDetail getNovelDetail(String url) {
        String jsonDetailString = getJsonString(url);

        NovelDetail novelDetail = getNovelDetailFromJsonString(jsonDetailString);
        if (novelDetail == null) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for url: " + url);
        }

        return novelDetail;
    }

    @Override
    public List<NovelSearchResult> search(String keyword, Integer page) {
        String searchUrl = buildSearchUrl(keyword, page);
        String jsonString = getJsonString(searchUrl);
        List<NovelSearchResult> novelSearchResults = getSearchDataFromJsonString(jsonString);
        if (novelSearchResults == null || novelSearchResults.isEmpty()) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for keyword: " + keyword);
        }
        return novelSearchResults;
    }

    public String getJsonString(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .execute();

            String jsonString = response.body();

            return jsonString;
        } catch (Exception e) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for url: " + url);
        }
    }

    protected abstract ChapterDetail getChapterDetailFromJsonString(String jsonChapterDetail);

    protected abstract List<ChapterInfo> getChapterListFromJsonString(String jsonChapterList);

    protected abstract NovelDetail getNovelDetailFromJsonString(String jsonDetail);

    protected abstract List<NovelSearchResult> getSearchDataFromJsonString(String jsonString);

    protected abstract String buildSearchUrl(String keyword, Integer page);

    protected abstract String buildNovelDetailUrl(Integer novelId, String type);

    protected abstract String buildChapterListUrlFromNovelDetailUrl(String url, Integer page);

    protected abstract String buildChapterDetailUrl(Integer chapterId);

    protected abstract void addMetaData(Map<String, Optional> map);

    protected List<NovelSearchResult> filterSearchResults(List<NovelSearchResult> novelSearchResults, String keyword) {
        return novelSearchResults;
    }
}
