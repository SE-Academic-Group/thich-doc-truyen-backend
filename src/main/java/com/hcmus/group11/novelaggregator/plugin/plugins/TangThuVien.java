package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.hcmus.group11.novelaggregator.plugin.BaseCrawler;
import com.hcmus.group11.novelaggregator.type.ChapterInfo;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.ResponseMetadata;
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

    @Override
    protected NovelDetail parseNovelDetailHTML(Document html) {
        String title = html.selectFirst("div.book-info h1").text();
        String author = html.selectFirst("div.book-info p.tag a.blue").text();
        String image = html.selectFirst("div.book-img img").attr("src");
        String url = html.baseUri();
        String nChapterText = html.selectFirst("a#j-bookCatalogPage").text().replaceAll("[^0-9]", "");
        Integer nChapter = Integer.parseInt(nChapterText);
        String description = html.selectFirst("div.book-intro p").text();
//        Remove all <br> from description
        description = description.replaceAll("<br>", "");

        Elements genreElements = html.select("div.book-info p.tag a.red");
        List<String> genres = new ArrayList<>();
        for (Element genreElement : genreElements) {
            genres.add(genreElement.text());
        }

        NovelDetail novelDetail = new NovelDetail(title, author, image, url, nChapter, description, genres, null);
        return novelDetail;
    }

    @Override
    protected ResponseMetadata parseSearchMetadata(Document html) {
        Elements pages = html.select("ul.pagination li a");
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

    @Override
    public List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page) {
        Document html = getHtml(novelDetailUrl);
        String storyId = html.selectFirst("input#story_id_hidden").attr("value");
        String url = pluginUrl + "/doc-truyen/page/" + storyId + "?page=" + page + "&limit=75&web=1";

        Document chapterListHtml = getHtml(url);
        
        System.out.println(chapterListHtml);

        List<ChapterInfo> chapterInfos = new ArrayList<>();


        return chapterInfos;
    }
}
