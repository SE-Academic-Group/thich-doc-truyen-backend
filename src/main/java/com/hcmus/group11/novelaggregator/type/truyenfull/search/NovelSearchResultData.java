package com.hcmus.group11.novelaggregator.type.truyenfull.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NovelSearchResultData {
    private int id;
    private String title;
    private String image;
    @JsonProperty("is_full")
    private boolean isFull;
    private String time;
    private String author;
    private String categories;
    @JsonProperty("category_ids")
    private String categoryIds;
    @JsonProperty("total_chapters")
    private int totalChapters;
}
