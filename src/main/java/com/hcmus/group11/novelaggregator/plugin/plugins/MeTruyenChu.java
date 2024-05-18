package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.hcmus.group11.novelaggregator.plugin.BaseCrawler;
import com.hcmus.group11.novelaggregator.type.ChapterInfo;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.ResponseMetadata;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import org.json.JSONObject;
import org.jsoup.Jsoup;
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
    protected NovelDetail parseNovelDetailHTML(Document html) {
        Elements div = html.select("div.book-info");

        String title = div.select("div.mRightCol h1").text();
        Elements lis = div.select("div.book-info ul li");
        String author = lis.get(0).select("a").text();
        String image = this.pluginUrl + div.select("div.book-info-pic img").attr("src");
        String nChapterText = lis.get(2).text();
        String url = html.baseUri();
        Integer nChapter = Integer.parseInt(nChapterText.replaceAll("[^0-9]", ""));
        String description = div.select("div.showmore div div").text();

        List<String> genres = new ArrayList<>();
        Elements li_genres = lis.get(1).select("li.li--genres a");
        for(Element li_genre : li_genres) {
            genres.add(li_genre.select("a").text());
        }



        return new NovelDetail(title, author, image, url, nChapter, description, genres);
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

    @Override
    public List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page) {
        Document html = getHtml(novelDetailUrl);
        Elements _a = html.select("div.paging a");

        String storyId = _a.get(1).attr("onclick");
        storyId = storyId.substring(storyId.indexOf('(') + 1, storyId.indexOf(','));
        String url = pluginUrl + "/get/listchap/" + storyId + "?page=" + page;

        try{
            String json = Jsoup.connect(url).ignoreContentType(true).execute().body();
            JSONObject jsonObject = new JSONObject(json);

            Document chapterListHtml = Jsoup.parse(jsonObject.getString("data"));

            List<ChapterInfo> chapterInfos = parseChapterListHTML(chapterListHtml);
            ResponseMetadata metadata = parseChapterListMetadata(chapterListHtml);
            metadata.addMetadataValue("currentPage", page);
            metadata.addMetadataValue("pluginName", pluginName);

            RequestAttributeUtil.setAttribute("metadata", metadata);

            return chapterInfos;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private List<ChapterInfo> parseChapterListHTML(Document html) {
        List<ChapterInfo> chapterInfos = new ArrayList<>();
        Elements chapterElements = html.select("div ul li");
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
            String url = pluginUrl + chapterElement.selectFirst("a").attr("href");
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
        ResponseMetadata responseMetadata = new ResponseMetadata();
        Integer maxPage = null;
        List<Integer> pageArray = null;


        Elements pages = html.select("div.paging a");
        Element onclickAttr = pages.last();
        System.out.println(onclickAttr);
        if(onclickAttr != null) {
            String lastPage = onclickAttr.attr("onclick");
            if(!lastPage.isEmpty()) {
                lastPage = lastPage.substring(lastPage.indexOf(',') + 1, lastPage.indexOf(')'));
            }else{
                lastPage = onclickAttr.text();
            }
            maxPage = (Integer.parseInt(lastPage));
            pageArray = new ArrayList<>();

            for(int i = 1; i <= maxPage; i++) {
                pageArray.add(i);
            }
        }
        responseMetadata.addMetadataValue("pageArray", pageArray);
        responseMetadata.addMetadataValue("maxPage", maxPage);
        return responseMetadata;
    }
}
