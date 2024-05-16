package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class BaseCrawler implements INovelPlugin {
    protected String pluginName;
    protected String pluginUrl;

    public String getPluginName() {
        return pluginName;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }

    @Override
    public List<NovelSearchResult> search(String keyword, Integer page) {
        String searchUrl = buildSearchUrl(keyword, page);
        Document html = getHtml(searchUrl);
        List<NovelSearchResult> novelSearchResults = parseSearchHTML(html);
        return novelSearchResults;
    }

    protected Document getHtml(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract String buildSearchUrl(String keyword, Integer page);

    protected abstract List<NovelSearchResult> parseSearchHTML(Document html);
}
