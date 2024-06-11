package com.hcmus.group11.novelaggregator.plugin.novel;

import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.type.*;
import com.hcmus.group11.novelaggregator.util.LevenshteinDistance;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class BaseCrawler implements INovelPlugin {
    protected String pluginName;
    protected String pluginUrl;

    @Override
    public String getPluginName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    public List<NovelSearchResult> search(String keyword, Integer page) {
        String searchUrl = buildSearchUrl(keyword, page);
        Document html = getHtml(searchUrl);

        List<NovelSearchResult> novelSearchResults = parseSearchHTML(html);
//        Filter out the novels
        if (novelSearchResults == null || novelSearchResults.isEmpty()) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for keyword: " + keyword);
        }
        ResponseMetadata metadata = parseSearchMetadata(html);
        metadata.addMetadataValue("currentPage", page);
        metadata.addMetadataValue("name", pluginName);

        RequestAttributeUtil.setAttribute("metadata", metadata);
        return novelSearchResults;
    }

    public NovelDetail getNovelDetail(String url) {
        Document html = getHtml(url);

        return parseNovelDetailHTML(html);
    }

    public ChapterDetail getChapterDetail(String url) {

        Document html = getHtml(url);
        return parseChapterDetailHTML(html);
    }

    protected String encodedUrl(String url) {
        try {
            int queryIndex = url.indexOf("?");
            String query = "";
            if (queryIndex != -1) {
                query = url.substring(queryIndex); // Lấy phần query
                url = url.substring(0, queryIndex); // Cắt bỏ phần query từ URL ban đầu
            }

            // Tìm vị trí dấu '/' đầu tiên sau phần "//" để tách base URL và path URL
            int indexOfSlash = url.indexOf("/", url.indexOf("//") + 2);
            if (indexOfSlash == -1 || indexOfSlash == url.length() - 1)
                throw new RuntimeException("The URL redirects to the homepage.");

            // Tách phần base URL và path URL
            String baseUrl = url.substring(0, indexOfSlash);
            String path = url.substring(indexOfSlash);

            // Mã hóa phần path
            String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8.toString())
                    .replace("%2F", "/");

            // Kết hợp lại URL đã mã hóa
            String encodedUrl = baseUrl + encodedPath + query;

            return encodedUrl;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected Document getHtml(String url) {
        try {
            Document doc = null;
            boolean redirected;
            String finalUrl = url;
            do {
                String encodedUrl = encodedUrl(finalUrl);

                // Thiết lập kết nối với các headers
                Connection connection = Jsoup.connect(encodedUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                        .followRedirects(false)
                        .timeout(5000);

                // Kiểm tra URL cuối cùng sau chuyển hướng
                Connection.Response response = connection.execute();
                finalUrl = response.header("Location");
                redirected = (finalUrl != null);

                if (!redirected) {
                    doc = connection.get();
                }
            } while (redirected);

            return doc;
        } catch (Exception e) {
            // Check if is 503 error
            if (e instanceof org.jsoup.HttpStatusException httpStatusException) {
                switch (httpStatusException.getStatusCode()) {
                    case 503:
                        throw HttpException.SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service is temporarily unavailable");
                    case 404:
                        throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for url: " + url);
                    default:
                        throw HttpException.BAD_REQUEST("BAD_REQUEST", "Bad request to url: " + url, e);
                }
            } else {
                throw HttpException.BAD_REQUEST("BAD_REQUEST", "Bad request to url: " + url, e);
            }
        }
    }

    protected Document getHtmlWithRetry(String url, int maxRetries, int retryDelay) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                return getHtml(url);
            } catch (HttpException e) {
                if (e.getStatusCode().is5xxServerError()) {
                    attempt++;
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        throw new RuntimeException(ie);
                    }
                    retryDelay *= 2; // Exponential backoff
                } else {
                    throw e;
                }
            }
        }
        throw HttpException.SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service is temporarily unavailable");
    }

    @Override
    public NovelSearchResult findSimilarNovel(String title, String author) {
        String searchUrl = buildSearchUrl(title, 1);
        Document html = getHtml(searchUrl);

        title = title.replace(" ", "");

        List<NovelSearchResult> novelSearchResults = parseSearchHTML(html);

        NovelSearchResult result = null;
        int bestDistance = Integer.MAX_VALUE;

        for (NovelSearchResult novelSearchResult : novelSearchResults) {
            String currentTitle = novelSearchResult.getTitle();
            String currentAuthor = novelSearchResult.getAuthor();

            if (currentTitle == null || currentAuthor == null) {
                continue;
            }

            String normalizedTitle = normalizeString(currentTitle, false);
            String normalizedAuthor = normalizeString(currentAuthor, false);

            int distanceTitle = LevenshteinDistance.computeLevenshteinDistance(normalizedTitle, title);
            int distanceAuthor = LevenshteinDistance.computeLevenshteinDistance(normalizedAuthor, author);

            if (distanceTitle < 5 && distanceAuthor < 2 && distanceTitle + distanceAuthor < bestDistance) {
                bestDistance = distanceAuthor + distanceTitle;
                result = novelSearchResult;
            }
        }

        return result;
    }

    @Override
    public ChapterInfo getChapterInfoByNovelUrlAndChapterIndex(String novelUrl, String chapterIndex) {
        ChapterInfo result = null;
        List<ChapterInfo> chapterInfoList = getFullChapterList(novelUrl);

        for (ChapterInfo chapterInfo : chapterInfoList) {
            if (chapterInfo.getIndex().equals(chapterIndex)) {
                result = chapterInfo;
                break;
            }
        }

        return result;
    }

    protected abstract String buildSearchUrl(String keyword, Integer page);

    protected abstract List<NovelSearchResult> parseSearchHTML(Document html);

    protected abstract NovelDetail parseNovelDetailHTML(Document html);

    protected ResponseMetadata parseSearchMetadata(Document html) {
        return null;
    }

    protected abstract ChapterDetail parseChapterDetailHTML(Document html);
}
