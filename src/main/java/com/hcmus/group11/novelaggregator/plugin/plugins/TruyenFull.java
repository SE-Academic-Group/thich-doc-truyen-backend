package com.hcmus.group11.novelaggregator.plugin.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.group11.novelaggregator.plugin.BaseApi;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.ResponseMetadata;
import com.hcmus.group11.novelaggregator.type.truyenfull.Response;
import com.hcmus.group11.novelaggregator.type.truyenfull.search.NovelSearchResultData;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TruyenFull extends BaseApi {

    public TruyenFull() {
        this.pluginName = "truyenFull";
        this.pluginUrl = "https://truyenfull.vn";
    }

    private final String SEARCH_URL = "https://api.truyenfull.vn/v1/tim-kiem?title={{keyword}}&page={{page}}";
    private final String NOVEL_DETAIL_BASE_URL = "https://api.truyenfull.vn/v1/story/detail/";

    @Override
    protected List<NovelSearchResult> getDataFromJsonString(String jsonString) {
        // Parse JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        Response searchResponse = null;
        try {
            searchResponse = objectMapper.readValue(jsonString, new TypeReference<Response<NovelSearchResultData>>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<NovelSearchResultData> dataList = searchResponse.getData();

        List<NovelSearchResult> results = new ArrayList<>();
        for (NovelSearchResultData data : dataList) {
            NovelSearchResult novelSearchResult = new NovelSearchResult(data.getTitle(), data.getAuthor(),
                    data.getImage(), buildNovelDetailUrl(data.getId(), null),
                    data.getTotalChapters());

            results.add(novelSearchResult);
        }

        Integer maxPage = searchResponse.getMeta().getPagination().getTotalPages();
        Integer currentPage = searchResponse.getMeta().getPagination().getCurrentPage();
        addMetaData(maxPage, currentPage);

        return results;
    }

    @Override
    protected String buildSearchUrl(String keyword, Integer page) {
        return SEARCH_URL
                .replace("{{keyword}}", keyword)
                .replace("{{page}}", page.toString());
    }

    @Override
    protected String buildNovelDetailUrl(Integer novelId, String type) {
        String result = NOVEL_DETAIL_BASE_URL + novelId;
        if (type != null) {
            result += "/" + type;
        }

        return result;
    }

    @Override
    protected void addMetaData(Integer maxPage, Integer currentPage) {
        ResponseMetadata metadata = new ResponseMetadata();
        metadata.addMetadataValue("maxPage", maxPage);
        metadata.addMetadataValue("currentPage", currentPage);
        metadata.addMetadataValue("pluginName", pluginName);

        RequestAttributeUtil.setAttribute("metadata", metadata);
    }
}
