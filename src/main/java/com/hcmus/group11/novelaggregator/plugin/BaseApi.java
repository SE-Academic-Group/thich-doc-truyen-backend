package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public abstract class BaseApi implements INovelPlugin{
    protected String pluginName;
    protected String pluginUrl;

    public String getPluginName() {
        return pluginName;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }

    @Override
    public List<NovelSearchResult> search(String keyword, Integer page){
        String searchUrl = buildSearchUrl(keyword, page);
        String jsonString = getJsonString(searchUrl);
        List<NovelSearchResult> novelSearchResults = getDataFromJsonString(jsonString);

        return novelSearchResults;
    }

    public String getJsonString(String url) {
        try{
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .execute();

            String jsonString = response.body();

            return jsonString;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract List<NovelSearchResult> getDataFromJsonString(String url);
    protected abstract String buildSearchUrl(String keyword, Integer page);
    protected abstract String buildNovelDetailUrl(Integer novelId, String type);
    protected abstract void addMetaData(Integer maxPage, Integer currentPage);

}
