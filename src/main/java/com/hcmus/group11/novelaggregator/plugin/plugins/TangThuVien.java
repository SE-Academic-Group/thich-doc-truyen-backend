package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.hcmus.group11.novelaggregator.plugin.BaseCrawler;
import com.hcmus.group11.novelaggregator.type.*;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
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
//        Remove all <br> from description with new line
        description = description.replaceAll("<br>", "\n");

        Elements genreElements = html.select("div.book-info p.tag a.red");
        List<String> genres = new ArrayList<>();
        for (Element genreElement : genreElements) {
            genres.add(genreElement.text());
        }

        NovelDetail novelDetail = new NovelDetail(title, author, image, url, nChapter, description, genres);
        return novelDetail;
    }

    @Override
    protected ResponseMetadata parseSearchMetadata(Document html) {
        Elements pages = html.select("ul.pagination li");
//        Get highest page number
        Integer maxPage = 1;
        for (Element page : pages) {
            Element pageElement = page.selectFirst("a") == null ? page.selectFirst("span") : page.selectFirst("a");
            if (pageElement == null) {
                continue;
            }
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
    protected ChapterDetail parseChapterDetailHTML(Document html) {
        String title = html.selectFirst("h2").text();
//        Replace all nbsp with space
        title = title.replaceAll("\u00A0", " ");
        String content = html.selectFirst(".box-chap").text();
        String url = html.baseUri();

        String prevPage, nextPage;
        // Get current id from url
        String currentIdStr = url.substring(url.lastIndexOf("-") + 1);
        Integer currentId = Integer.parseInt(currentIdStr);
        // Get prevPage and nextPage
        if (currentId == 1) {
            prevPage = null;
        } else {
            prevPage = url.substring(0, url.lastIndexOf("-") + 1) + (currentId - 1);
        }
        nextPage = url.substring(0, url.lastIndexOf("-") + 1) + (currentId + 1);


        ChapterDetail chapterDetail = new ChapterDetail(title, url, content);
        ResponseMetadata metadata = new ResponseMetadata();
        metadata.addMetadataValue("prevPage", prevPage);
        metadata.addMetadataValue("nextPage", nextPage);
        metadata.addMetadataValue("pluginName", pluginName);
        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterDetail;
    }

    @Override
    public List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page) {
        page = Math.max(page - 1, 0);
        Document html = getHtml(novelDetailUrl);
        String storyId = html.selectFirst("input#story_id_hidden").attr("value");
        String url = pluginUrl + "/doc-truyen/page/" + storyId + "?page=" + page + "&limit=75&web=1";

        Document chapterListHtml = getHtml(url);

        List<ChapterInfo> chapterInfos = parseChapterListHTML(chapterListHtml);
        ResponseMetadata metadata = parseChapterListMetadata(chapterListHtml);
        metadata.addMetadataValue("currentPage", page + 1);
        metadata.addMetadataValue("pluginName", pluginName);

        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterInfos;
    }

    private List<ChapterInfo> parseChapterListHTML(Document html) {
        List<ChapterInfo> chapterInfos = new ArrayList<>();
        Elements chapterElements = html.select("ul.cf li");
        for (Element chapterElement : chapterElements) {
            Element chapterTitleElement = chapterElement.selectFirst("a");

            // If chapterTitleElement is null => this "li" is the Divider chap => no url, just title and span
            if (chapterTitleElement == null) {
                ChapterInfo chapterInfo = new ChapterInfo();
                chapterInfo.setTitle(chapterElement.selectFirst("span").text());
                chapterInfos.add(chapterInfo);
                continue;
            }


            String title = chapterElement.selectFirst("a").text();
            String url = chapterElement.selectFirst("a").attr("href");
//            Title format is "Chương {chapterIndex} : {chapterTitle}"
            String chapterIndexText = title.split(":")[0].replaceAll("[^0-9]", "");
            Integer chapterIndex = Integer.parseInt(chapterIndexText);
            title = title.split(":")[1].trim();

            ChapterInfo chapterInfo = new ChapterInfo(title, url, chapterIndex);
            chapterInfos.add(chapterInfo);
        }

        return chapterInfos;
    }

    private ResponseMetadata parseChapterListMetadata(Document html) {
        Elements pages = html.select("ul.pagination li a");
//        Get page array
        List<Integer> pageArray = new ArrayList<>();
        Integer maxPage = 1;
        for (Element page : pages) {
            String pageNumber = page.text();
            pageNumber = pageNumber.replaceAll("[^0-9]", "");
            if (pageNumber.isEmpty()) {
                continue;
            }
            pageArray.add(Integer.parseInt(pageNumber));
            maxPage = Math.max(maxPage, Integer.parseInt(pageNumber));
        }

        ResponseMetadata responseMetadata = new ResponseMetadata();
        responseMetadata.addMetadataValue("pageArray", pageArray);
        responseMetadata.addMetadataValue("maxPage", maxPage);
        return responseMetadata;
    }
}
