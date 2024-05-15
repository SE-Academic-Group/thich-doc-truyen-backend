package com.hcmus.group11.novelaggregator.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NovelSearchResult {
    private String title;
    private String author;
    private String image;
    private String url;
    private Integer nChapter;
}
