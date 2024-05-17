package com.hcmus.group11.novelaggregator.plugin;

import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.ResponseMetadata;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
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
        ResponseMetadata metadata = parseSearchMetadata(html);
        metadata.addMetadataValue("currentPage", page);
        metadata.addMetadataValue("pluginName", pluginName);

        RequestAttributeUtil.setAttribute("metadata", metadata);
        return novelSearchResults;
    }

    public NovelDetail getNovelDetail(String url) {
        Document html = getHtml(url);

        return parseNovelDetailHTML(html);
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

    protected abstract NovelDetail parseNovelDetailHTML(Document html);

    protected ResponseMetadata parseSearchMetadata(Document html) {
        return null;
    }

}
