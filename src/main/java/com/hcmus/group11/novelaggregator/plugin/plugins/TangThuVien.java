package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.hcmus.group11.novelaggregator.plugin.BaseCrawler;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TangThuVien extends BaseCrawler {
    public TangThuVien() {
        pluginName = "tangThuVien";
        pluginUrl = "https://truyen.tangthuvien.vn";
    }

    @Override
    public String buildSearchUrl(String keyword, Integer page) {
        return pluginUrl + "/ket-qua-tim-kiem?term=" + keyword + "&page=" + page;
    }

    @Override
    protected List<NovelSearchResult> parseSearchHTML(Document html) {
        List<NovelSearchResult> novelSearchResults = new ArrayList<>();

        //        Get ul child from div.book-img-text
        Elements lis = html.select("div.book-img-text ul li");
        System.out.println(lis);
        for (Element li : lis) {
            // Extract information
            String title = li.selectFirst("div.book-mid-info h4 a").text();
            String author = li.selectFirst("p.author a.name").text();
            String image = li.selectFirst("div.book-img-box img").attr("src");
            String url = li.selectFirst("div.book-mid-info h4 a").attr("href");
            String nChapterText = li.selectFirst("p.author span.KIBoOgno").text();
            Integer nChapter = Integer.parseInt(nChapterText);

            NovelSearchResult novelSearchResult = new NovelSearchResult(title, author, image, url, nChapter);
            novelSearchResults.add(novelSearchResult);
        }

        return novelSearchResults;
    }
}
