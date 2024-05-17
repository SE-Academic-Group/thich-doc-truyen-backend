package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.group11.novelaggregator.plugin.BaseApi;
import com.hcmus.group11.novelaggregator.type.ChapterInfo;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.ResponseMetadata;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import org.springframework.stereotype.Component;
import java.util.Optional;

import java.util.*;

@Component
public class TruyenFull extends BaseApi {

    public TruyenFull() {
        this.pluginName = "truyenFull";
        this.pluginUrl = "https://truyenfull.vn";
    }

    public final String BASE_URL = "https://api.truyenfull.vn/v1";
    public final String SEARCH_URL = BASE_URL + "/tim-kiem?title={{keyword}}&page={{page}}";
    public final String NOVEL_DETAIL_BASE_URL = BASE_URL+ "/story/detail/";
    public final String CHAPTER_DETAIL_BASE_URL = BASE_URL + "/chapter/detail/";

    @Override
    protected List<ChapterInfo> getChapterListFromJsonString(String jsonChapterList) {
        List<ChapterInfo> chapterList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonChapterList, Map.class);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonMap.get("data");

            Map<String, Object> meta = (Map<String, Object>) jsonMap.get("meta");
            Map<String, Object> pagination = (Map<String, Object>) meta.get("pagination");
            Map<String, Object> links = (Map<String, Object>) pagination.get("links");

            Integer perPage = (Integer) pagination.get("per_page");
            Integer maxPage = (Integer) pagination.get("total_pages");
            Integer currentPage = (Integer) pagination.get("current_page");
            String nextPageUrl = (String) links.get("next");
            String prevPageUrl = (String) links.get("previous");

            Integer startId = (currentPage - 1) * perPage + 1;
            for (Map<String, Object> data : dataList) {
                ChapterInfo chapterInfo = new ChapterInfo();
                chapterInfo.setTitle((String) data.get("title"));
                chapterInfo.setUrl(buildChapterDetailUrl((Integer) data.get("id")));
                chapterInfo.setIndex(startId ++);

                chapterList.add(chapterInfo);
            }

            Map<String, Optional> map = new HashMap<>();
            map.put("maxPage", Optional.ofNullable(maxPage));
            map.put("currentPage", Optional.ofNullable(currentPage));
            map.put("prevPageUrl", Optional.ofNullable(prevPageUrl));
            map.put("nextPageUrl", Optional.ofNullable(nextPageUrl));

            addMetaData(map);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return chapterList;
    }

    @Override
    protected NovelDetail getNovelDetailFromJsonString(String jsonDetail) {
        NovelDetail novelDetail = new NovelDetail();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonDetail, Map.class);
            Map<String, Object> data = (Map<String, Object>) jsonMap.get("data");

            novelDetail.setTitle((String) data.get("title"));
            novelDetail.setAuthor((String) data.get("author"));
            novelDetail.setImage((String) data.get("image"));
            novelDetail.setUrl(buildNovelDetailUrl( (Integer) data.get("id"), null));
            novelDetail.setNChapter((Integer) data.get("total_chapters"));
            novelDetail.setDescription((String) data.get("description"));

            List<String> listCategories = Arrays.asList(((String) data.get("categories")).split(",\\s*"));
            novelDetail.setGenres(listCategories);


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return novelDetail;
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
                novelSearchResult.setUrl(buildNovelDetailUrl( (Integer) data.get("id"), null));
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
    protected String buildNovelDetailUrl( Integer id, String type) {

        String result = NOVEL_DETAIL_BASE_URL + id;

        if (type != null) {
            result += "/" + type;
        }

        return result;
    }

    @Override
    protected String buildChapterListUrlFromNovelDetailUrl(String url, Integer page) {
        String result = url;
        if(!url.contains("/chapters?page"))
        {
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
}
