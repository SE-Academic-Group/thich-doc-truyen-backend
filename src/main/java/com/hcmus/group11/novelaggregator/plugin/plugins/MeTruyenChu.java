package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.hcmus.group11.novelaggregator.plugin.BaseCrawler;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.ResponseMetadata;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MeTruyenChu extends BaseCrawler {
    public MeTruyenChu() {
        pluginName = "meTruyenChu";
        pluginUrl = "https://metruyenchu.com.vn";
    }

    @Override
    public String buildSearchUrl(String keyword, Integer page) {
        return pluginUrl + "/search?q=" + keyword + "&page=" + page;
    }

    @Override
    protected List<NovelSearchResult> parseSearchHTML(Document html) {
        List<NovelSearchResult> novelSearchResults = new ArrayList<>();

        //        Get ul child from div.book-img-text
        Elements divs = html.select("div.truyen-list div.item");
        for (Element div : divs) {
            // Extract information
            String title = div.selectFirst("h3 a").text();
            Elements lines = div.select("p.line");
            String author = lines.get(0).selectFirst("a").text();
            String image = this.pluginUrl + div.selectFirst("a.cover img").attr("src");
            String url = this.pluginUrl + div.selectFirst("a.cover").attr("href");
            String nChapterText = lines.get(2).text();
            Integer nChapter = Integer.parseInt(nChapterText.replaceAll("[^0-9]", ""));

            NovelSearchResult novelSearchResult = new NovelSearchResult(title, author, image, url, nChapter);
            novelSearchResults.add(novelSearchResult);
        }

        return novelSearchResults;
    }

    @Override
    protected ResponseMetadata parseSearchMetadata(Document html) {
        Elements pages = html.select("div.phan-trang a");
//        Get highest page number
        Integer maxPage = 1;
        for (Element page : pages) {
            String pageNumber = page.text();
            pageNumber = pageNumber.replaceAll("[^0-9]", "");
            if (pageNumber.isEmpty()) {
                continue;
            }
            maxPage = Math.max(maxPage, Integer.parseInt(pageNumber));
        }

        ResponseMetadata responseMetadata = new ResponseMetadata();
        responseMetadata.addMetadataValue("maxPage", maxPage);

        return responseMetadata;
    }
}
