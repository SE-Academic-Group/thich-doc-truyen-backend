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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ThichTruyen extends BaseCrawler {

    public ThichTruyen() {
        pluginName = "thichTruyen";
        pluginUrl = "https://thichtruyen.vn";
    }

    @Override
    public String buildSearchUrl(String keyword, Integer page) {
        return pluginUrl + "/tim-kiem?q=" + keyword + "&page=" + page;
    }

    @Override
    protected List<NovelSearchResult> parseSearchHTML(Document html) {
        List<NovelSearchResult> novelSearchResults = new ArrayList<>();

        //        Get ul child from div.book-img-text
        Elements lis = html.select("div.view-category-item");
        for (Element li : lis) {
            // Extract information
            String title = li.selectFirst("h3.view-category-item-title").text();
            String url = pluginUrl + "/" + li.selectFirst("a").attr("href");

            String author = li.selectFirst("p.view-category-item-author a").text();
            String image = pluginUrl + "/" + li.selectFirst("div.view-category-item-image img").attr("src");
            Elements infor_list = li.select("div.view-category-item-infor p");
            String nChapterText = infor_list.get(2).text().split(" ")[0];
            Integer nChapter = Integer.parseInt(nChapterText);

            NovelSearchResult novelSearchResult = new NovelSearchResult(title, author, image, url, nChapter);
            novelSearchResults.add(novelSearchResult);
        }

        return novelSearchResults;
    }

    @Override
    protected NovelDetail parseNovelDetailHTML(Document html) {
        String title = html.selectFirst("h1.story-intro-title").text();
        String author = html.selectFirst("p.story-intro-author a").text();
        String image = pluginUrl + "/" + html.selectFirst("div.story-intro-image img").attr("src");
        String url = html.baseUri() +  html.selectFirst("h1.story-intro-title a").attr("href");
        String nChapterText = html.selectFirst("p.story-intro-chapper").text().replaceAll("[^0-9]", "");
        Integer nChapter = Integer.parseInt(nChapterText);
        String description = html.selectFirst("div#tab-over div.tab-text p").text();
        if(description.equals("Đọc Truyện")){
            description = html.selectFirst("div#tab-over div.tab-text").text();
        }
// Loại bỏ tất cả <br> trong mô tả và thay bằng dòng mới
        description = description.replaceAll("<br>", "\n");
        description = description.replaceAll("(?i)</?strong>", "");
        description = description.replaceAll("\u00A0", " ");

// Lấy thể loại
        Element lst_tag = html.selectFirst("div.lst-tag");
        Elements genreElements = lst_tag.select("a");
        List<String> genres = new ArrayList<>();
        for (Element genreElement : genreElements) {
            genres.add(genreElement.text());
        }

        NovelDetail novelDetail = new NovelDetail(title, author, image, url, nChapter, description, genres);
        return novelDetail;
    }

    @Override
    protected ResponseMetadata parseSearchMetadata(Document html) {
        Elements pages = html.select("div.pagination a");
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

        String currentPageString = html.selectFirst("div.pagination strong").text();
        Integer currentPage = Integer.parseInt(currentPageString);

        if(currentPage > maxPage) {
            maxPage = currentPage;
        }

        ResponseMetadata responseMetadata = new ResponseMetadata();
        responseMetadata.addMetadataValue("maxPage", maxPage);

        return responseMetadata;
    }

    @Override
    protected ChapterDetail parseChapterDetailHTML(Document html) {
        Elements lis = html.select("div.main-breadcrumb ul li");
        String novelTitle = lis.get(2).selectFirst("a").text();
        String url = pluginUrl + "/" + lis.get(2).selectFirst("a").attr("href");

        String title = html.selectFirst("div.story-detail-header h1").text();
//        Replace all nbsp with space
        title = title.replaceAll("\u00A0", " ");
        String content = html.selectFirst("div.story-detail-content").text();
        content = content.replaceAll("(?i)<br\\s*/?>", "");

        ChapterDetail chapterDetail = new ChapterDetail(novelTitle,title, url, content);

        String nextChapter = html.selectFirst("div.next-previous a").attr("href");
        if(nextChapter.contains("javascript")){
            nextChapter = null;
        }
        else{
            nextChapter = pluginUrl + "/" + nextChapter;
        }

        String previousChapter = html.selectFirst("div.prev-previous a").attr("href");
        if(previousChapter.contains("javascript")){
            previousChapter = null;
        }
        else{
            previousChapter = pluginUrl + "/" + previousChapter;
        }

        ResponseMetadata metadata = new ResponseMetadata();
        metadata.addMetadataValue("nextPage", nextChapter);
        metadata.addMetadataValue("prevPage", previousChapter);
        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterDetail;
    }


    @Override
    public List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page) {

        Document chapterListHtml = getHtml(novelDetailUrl);

        List<ChapterInfo> chapterInfos = parseChapterListHTML(chapterListHtml);
        ResponseMetadata metadata = parseChapterListMetadata(chapterListHtml);
        metadata.addMetadataValue("currentPage", page);
        metadata.addMetadataValue("pluginName", pluginName);

        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterInfos;
    }

    private List<ChapterInfo> parseChapterListHTML(Document html) {
        List<ChapterInfo> chapterInfos = new ArrayList<>();
        Elements chapterElements = html.select("div.tab-text ul li");

        Integer chapterIndex = 1;
        for (Element chapterElement : chapterElements) {
            Element chapterInfoElement = chapterElement.selectFirst("a");

            String title = chapterInfoElement.attr("title");
            String url = pluginUrl + "/" + chapterInfoElement.attr("href");

            ChapterInfo chapterInfo = new ChapterInfo(title, url, chapterIndex ++);
            chapterInfos.add(chapterInfo);
        }

        return chapterInfos;
    }

    private ResponseMetadata parseChapterListMetadata(Document html) {
        Elements pages = html.select("div.pagination a");
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
