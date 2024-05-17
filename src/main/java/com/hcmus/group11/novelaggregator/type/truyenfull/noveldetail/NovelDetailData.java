package com.hcmus.group11.novelaggregator.type.truyenfull.noveldetail;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NovelDetailData {
    private Integer id;
    private String title;
    private String image;
    private String status;
    private String author;
    @JsonProperty("total_chapters")
    private Integer totalChapters;
    @JsonProperty("total_like")
    private Integer totalLike;
    @JsonProperty("total_view")
    private String totalView;
    private String categories;
    @JsonProperty("category_ids")
    private String categoryIds;
    @JsonProperty("chapters_new")
    private String chaptersNew;
    private String description;

}
