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
public class TrumTruyen extends BaseCrawler {

    public TrumTruyen() {
        pluginName = "trumTruyen";
        pluginUrl = "https://trumtruyen.vn";
    }

    @Override
    public String buildSearchUrl(String keyword, Integer page) {
        return pluginUrl + "/tim-kiem?tukhoa=" + keyword + "&page=" + page;
    }

    @Override
    protected List<NovelSearchResult> parseSearchHTML(Document html) {
        List<NovelSearchResult> novelSearchResults = new ArrayList<>();

        // Select all the rows that contain novel information
        Elements rows = html.select("div.col-truyen-main div.list-main div.row");
        for (Element row : rows) {
            // Extract information
            String title = row.selectFirst("h3.truyen-title a").text();
            String author = row.select("span.author").get(0).text();
            String image = row.select("img").attr("src");

            if(image == ""){
                image = row.select("div.col-list-image div div").attr("data-image");
            }

            String url = row.selectFirst("h3.truyen-title a").attr("href");

            // Get the number of chapters
            String nChapterText = row.select("span.author").get(1).text().split(" ")[0];
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
        Element lastPageElement = html.select("ul.pagination li a:contains(Cuối)").first();

        Integer maxPage = 1;
        if (lastPageElement != null) {
            String href = lastPageElement.attr("href");
            // Tìm số trang cuối cùng từ thuộc tính href
            String pageStr = href.substring(href.indexOf("page=") + 5);
            try{
                maxPage = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                maxPage = 1;
            }
        }

        Elements paginationElements = html.select("ul.pagination li a");
        int lastPage = 1;

        for (Element pageElement : paginationElements) {
            String pageHref = pageElement.attr("href");
            String pageNumberStr = pageHref.substring(pageHref.indexOf("page=") + 5);

            try {
                int pageNumber = Integer.parseInt(pageNumberStr);
                if (pageNumber > lastPage) {
                    lastPage = pageNumber;
                }
            } catch (NumberFormatException e) {
                lastPage = 1;
            }
        }

        if(lastPage > maxPage){
            maxPage = lastPage;
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

        ChapterDetail chapterDetail = new ChapterDetail(title, url, content);
        return chapterDetail;
    }

    @Override
    public List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page) {
        Document html = getHtml(novelDetailUrl);
        String storyId = html.selectFirst("input#story_id_hidden").attr("value");
        String url = pluginUrl + "/doc-truyen/page/" + storyId + "?page=" + page + "&limit=75&web=1";

        Document chapterListHtml = getHtml(url);

        List<ChapterInfo> chapterInfos = parseChapterListHTML(chapterListHtml);
        ResponseMetadata metadata = parseChapterListMetadata(chapterListHtml);
        metadata.addMetadataValue("currentPage", page);
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
