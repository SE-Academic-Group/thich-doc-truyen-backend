package com.hcmus.group11.novelaggregator.type;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class NovelDetail extends NovelSearchResult {
    private String description;
    private List<String> genres;

    public NovelDetail(String title, String author, String image, String url, Integer nChapter, String description, List<String> genres) {
        super(title, author, image, url, nChapter);
        this.description = description;
        this.genres = genres;
    }
}
