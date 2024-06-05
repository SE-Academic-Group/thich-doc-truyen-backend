package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.plugin.BaseCrawler;
import com.hcmus.group11.novelaggregator.type.*;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        try {
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
        } catch (Exception e) {
            return novelSearchResults;
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
        String novelTitle = null;

        String title = null;
        String content = null;
        try {
            novelTitle = html.selectFirst("h1.truyen-title a").text();

            title = html.selectFirst("h2").text();
            //        Replace all nbsp with space
            title = title.replaceAll("\u00A0", " ");
            content = html.selectFirst(".box-chap").text();
        } catch (Exception e) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "Chapter not found");
        }

        String url = html.baseUri();

        String prevPage, nextPage;
        // Get current id from url
        String currentIdStr = url.substring(url.lastIndexOf("-") + 1);
        Integer currentId = Integer.parseInt(currentIdStr);
        // Get prevPage and nextPage
        if (currentId == 1) {
            prevPage = null;
        } else {
            prevPage = url.substring(0, url.lastIndexOf("/") + 1) + "chuong-" + (currentId - 1);
        }
        nextPage = url.substring(0, url.lastIndexOf("/") + 1) + "chuong-" + (currentId + 1);


        ChapterDetail chapterDetail = new ChapterDetail(novelTitle, title, url, content);
        ResponseMetadata metadata = new ResponseMetadata();
        metadata.addMetadataValue("prevPage", prevPage);
        metadata.addMetadataValue("nextPage", nextPage);
        metadata.addMetadataValue("name", pluginName);
        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterDetail;
    }

    @Override
    public List<ChapterInfo> getChapterList(String novelDetailUrl, Integer page) {
        page = Math.max(page - 1, 0);
        Document html = getHtml(novelDetailUrl);
        String storyId = html.selectFirst("input#story_id_hidden").attr("value");
        String url = this.pluginUrl + "/doc-truyen/page/" + storyId + "?page=" + page + "&limit=75&web=1";

        Document chapterListHtml = getHtml(url);

        List<ChapterInfo> chapterInfos = parseChapterListHTML(chapterListHtml);
        ResponseMetadata metadata = parseChapterListMetadata(chapterListHtml);
        metadata.addMetadataValue("currentPage", page + 1);
        metadata.addMetadataValue("name", pluginName);

        RequestAttributeUtil.setAttribute("metadata", metadata);

        return chapterInfos;
    }

    @Override
    public List<ChapterInfo> getFullChapterList(String novelUrl) {
        Integer page = 0;
        Document html = getHtml(novelUrl);
        String storyId = html.selectFirst("input#story_id_hidden").attr("value");
        Integer MaxPage = maxChapterListPage(html);

        List<CompletableFuture<List<ChapterInfo>>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<ChapterInfo> result = new ArrayList<>();

        for (Integer i = 0; i <= MaxPage; i++) {
            page = i;
            String urlPage = this.pluginUrl + "/doc-truyen/page/" + storyId + "?page=" + page + "&limit=75&web=1";

            CompletableFuture<List<ChapterInfo>> future = CompletableFuture.supplyAsync(() -> getHtml(urlPage), executor)
                    .thenApply(this::parseChapterListHTML)
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return new ArrayList<ChapterInfo>();
                    });

            futures.add(future);
        }

        List<List<ChapterInfo>> chapterInfos = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        for (List<ChapterInfo> chapterInfoList : chapterInfos) {
            result.addAll(chapterInfoList);
        }

        executor.shutdown();
        return result;
    }

    public Integer maxChapterListPage(Document html) {
        Elements paginationItems = html.select("ul.pagination li a");

        int lastLoadingNumber = -1;
        int maxLoadingNumber = -1;

        for (Element item : paginationItems) {
            String onclickAttr = item.attr("onclick");
            if (onclickAttr.startsWith("Loading(")) {
                String numberString = onclickAttr.replaceAll("[^0-9]", "");
                int number = Integer.parseInt(numberString);

                maxLoadingNumber = Math.max(maxLoadingNumber, number);

                if (item.text().contains("Trang cuối")) {
                    lastLoadingNumber = number;
                    break;
                }
            }
        }

        int result = (lastLoadingNumber != -1) ? lastLoadingNumber : maxLoadingNumber;

        return result;

    }

    private List<ChapterInfo> parseChapterListHTML(Document html) {
        List<ChapterInfo> chapterInfos = new ArrayList<>();
        Elements chapterElements = html.select("ul li");
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
            int startIndex = title.indexOf("Chương") + "Chương".length() + 1;
            int endIndex = title.indexOf(":");
            String chapterIndex = "";
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                chapterIndex = title.substring(startIndex, endIndex).trim();
            } else {
                continue;
            }

            if (chapterIndex.endsWith(" ")) {
                chapterIndex = chapterIndex.substring(0, chapterIndex.length() - 1);
            }

            if (endIndex != -1 && endIndex < title.length() - 1) {
                title = title.split(":")[1].trim();
            }

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
