package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.group11.novelaggregator.exception.type.HttpException;
import com.hcmus.group11.novelaggregator.plugin.BaseApi;
import com.hcmus.group11.novelaggregator.type.*;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TruyenFull extends BaseApi {

    public TruyenFull() {
        this.pluginName = "truyenFull";
        this.pluginUrl = "https://truyenfull.vn";
    }

    public final String BASE_URL = "https://api.truyenfull.vn/v1";
    public final String SEARCH_URL = BASE_URL + "/tim-kiem?title={{keyword}}&page={{page}}";
    public final String NOVEL_DETAIL_BASE_URL = BASE_URL + "/story/detail/";
    public final String CHAPTER_DETAIL_BASE_URL = BASE_URL + "/chapter/detail/";

    @Override
    protected ChapterDetail getChapterDetailFromJsonString(String jsonChapterDetail) {
        ChapterDetail chapterDetail = new ChapterDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonChapterDetail, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");

            if (status.equals("error")) {
                return null;
            } else {
                chapterDetail.setNovelTitle((String) data.get("story_name"));
                chapterDetail.setTitle((String) data.get("chapter_name"));

                String content = (String) data.get("content");
                chapterDetail.setContent(content);

                Integer id = (Integer) data.get("chapter_id");
                chapterDetail.setUrl(buildChapterDetailUrl(id));

                Integer nextChapterId = (Integer) data.get("chapter_next");
                Integer prevChapterId = (Integer) data.get("chapter_prev");

                Map<String, Optional> map = new HashMap<>();

                String nextChapterDetailUrl = null;
                if (nextChapterId != null) {
                    nextChapterDetailUrl = buildChapterDetailUrl(nextChapterId);
                }

                String prevChapterDetailUrl = null;
                if (prevChapterId != null) {
                    prevChapterDetailUrl = buildChapterDetailUrl(prevChapterId);
                }

                map.put("nextPage", Optional.ofNullable(nextChapterDetailUrl));

                map.put("prevPage", Optional.ofNullable(prevChapterDetailUrl));

                addMetaData(map);

                return chapterDetail;

            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected List<ChapterInfo> getChapterListFromJsonString(String jsonChapterList) {
        List<ChapterInfo> chapterList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonChapterList, Map.class);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");

            if (status.equals("error")) {
                return null;
            } else {
                Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
                Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");
                Map<String, Object> links = (Map<String, Object>) pagination.get("links");

                Integer perPage = (Integer) pagination.get("per_page");
                Integer maxPage = (Integer) pagination.get("total_pages");
                Integer currentPage = (Integer) pagination.get("current_page");
                //            String nextPageUrl = (String) links.get("next");
                //            String prevPageUrl = (String) links.get("previous");

                Integer startId = (currentPage - 1) * perPage;
                for (Map<String, Object> data : dataList) {
                    ChapterInfo chapterInfo = new ChapterInfo();
                    chapterInfo.setTitle((String) data.get("title"));
                    chapterInfo.setUrl(buildChapterDetailUrl((Integer) data.get("id")));
                    startId++;
                    chapterInfo.setIndex(startId.toString());

                    chapterList.add(chapterInfo);
                }

                Map<String, Optional> map = new HashMap<>();
                map.put("maxPage", Optional.ofNullable(maxPage));
                map.put("currentPage", Optional.ofNullable(currentPage));
                //            map.put("prevPageUrl", Optional.ofNullable(prevPageUrl));
                //            map.put("nextPageUrl", Optional.ofNullable(nextPageUrl));

                addMetaData(map);
                return chapterList;

            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected NovelDetail getNovelDetailFromJsonString(String jsonDetail) {
        NovelDetail novelDetail = new NovelDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonDetail, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");
            String status = (String) jsonMap.get("status");
            if (status.equals("error")) {
                return null;
            } else {
                novelDetail.setTitle((String) data.get("title"));
                novelDetail.setAuthor((String) data.get("author"));
                novelDetail.setImage((String) data.get("image"));
                novelDetail.setUrl(buildNovelDetailUrl((Integer) data.get("id"), null));
                novelDetail.setNChapter((Integer) data.get("total_chapters"));
                String description = (String) data.get("description");
                novelDetail.setDescription(description);

                List<String> listCategories = Arrays.asList(((String) data.get("categories")).split(",\\s*"));
                novelDetail.setGenres(listCategories);
                return novelDetail;
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected List<NovelSearchResult> getSearchDataFromJsonString(String jsonString) {
        List<NovelSearchResult> novelSearchResults = new ArrayList<>();
        // Parse JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonString, Map.class);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonMap.get("data");

            for (Map<String, Object> data : dataList) {
                NovelSearchResult novelSearchResult = new NovelSearchResult();
                novelSearchResult.setTitle((String) data.get("title"));
                novelSearchResult.setAuthor((String) data.get("author"));
                novelSearchResult.setImage((String) data.get("image"));
                novelSearchResult.setUrl(buildNovelDetailUrl((Integer) data.get("id"), null));
                novelSearchResult.setNChapter((Integer) data.get("total_chapters"));

                novelSearchResults.add(novelSearchResult);

            }

            Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
            Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");

            Integer maxPage = (Integer) pagination.get("total_pages");
            Integer currentPage = (Integer) pagination.get("current_page");
            Map<String, Optional> map = new HashMap<>();
            map.put("maxPage", Optional.ofNullable(maxPage));
            map.put("currentPage", Optional.ofNullable(currentPage));

            addMetaData(map);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return novelSearchResults;
    }

    @Override
    protected String buildSearchUrl(String keyword, Integer page) {
        return SEARCH_URL
                .replace("{{keyword}}", keyword)
                .replace("{{page}}", page.toString());
    }

    @Override
    protected String buildNovelDetailUrl(Integer id, String type) {

        String result = NOVEL_DETAIL_BASE_URL + id;

        if (type != null) {
            result += "/" + type;
        }

        return result;
    }

    @Override
    protected String buildChapterListUrlFromNovelDetailUrl(String url, Integer page) {
        String result = url;
        if (!url.contains("/chapters?page")) {
            result += "/chapters?page=" + page;
        }

        return result;
    }

    @Override
    protected String buildChapterDetailUrl(Integer chapterId) {
        return CHAPTER_DETAIL_BASE_URL + chapterId;
    }

    @Override
    protected void addMetaData(Map<String, Optional> map) {
        ResponseMetadata metadata = new ResponseMetadata();
        for (Map.Entry<String, Optional> entry : map.entrySet()) {
            metadata.addMetadataValue(entry.getKey(), entry.getValue());
        }

        RequestAttributeUtil.setAttribute("metadata", metadata);
    }

    @Override
    public List<ChapterInfo> getFullChapterList(String url) {
        try {
            Integer currentPage = 1;

            // Get first page to get total pages
            String firstChapterListUrl = buildChapterListUrlFromNovelDetailUrl(url, 1);
            String firstJsonChapterListString = getJsonString(firstChapterListUrl);

            Integer maxPage = getMaxPage(firstJsonChapterListString);
            List<CompletableFuture<List<ChapterInfo>>> futures = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<ChapterInfo> result = new ArrayList<>();

            // Add first page to result
            result.addAll(parseChapterListJson(firstJsonChapterListString, 1, url));

            // Use CompletableFuture to get all other pages concurrently
            for (int i = 2; i <= maxPage; i++) {
                int page = i;
                String chapterListUrl = buildChapterListUrlFromNovelDetailUrl(url, page);
                CompletableFuture<List<ChapterInfo>> future = CompletableFuture.supplyAsync(() -> getJsonString(chapterListUrl), executor).thenApply(json -> parseChapterListJson(json, page, url));
                futures.add(future);
            }

            // Wait for all futures to complete
            List<List<ChapterInfo>> chapterInfos = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            for (List<ChapterInfo> chapterInfoList : chapterInfos) {
                result.addAll(chapterInfoList);
            }

            executor.shutdown();

            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ChapterInfo> parseChapterListJson(String json, Integer currentPage, String url) {
        List<ChapterInfo> chapterList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = null;
        try {
            jsonMap = objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for novel url: " + url + " page: " + currentPage);
        }
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonMap.get("data");

        String status = (String) jsonMap.get("status");
        if (status.equals("error")) {
            throw HttpException.NOT_FOUND("NOT_FOUND", "No result found for novel url: " + url + " page: " + currentPage);
        } else {

            Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
            Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");

            Integer perPage = (Integer) pagination.get("per_page");

            Integer startId = (currentPage - 1) * perPage + 1;
            for (Map<String, Object> data : dataList) {
                ChapterInfo chapterInfo = new ChapterInfo();
                chapterInfo.setTitle((String) data.get("title"));
                chapterInfo.setUrl(buildChapterDetailUrl((Integer) data.get("id")));

                chapterInfo.setIndex(startId.toString());
                startId++;

                chapterList.add(chapterInfo);
            }
        }

        return chapterList;
    }

    private Integer getMaxPage(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);
        Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
        Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");

        return (Integer) pagination.get("total_pages");
    }
}
