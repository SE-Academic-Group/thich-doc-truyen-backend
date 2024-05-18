package com.hcmus.group11.novelaggregator.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Details of a novel")
public class NovelDetail extends NovelSearchResult {

    @Schema(description = "Description of the novel", example = "This is a novel")
    private String description;

    @Schema(description = "List of genres of the novel", example = "[\"Fantasy\", \"Adventure\"]")
    private List<String> genres;

    public NovelDetail(String title, String author, String image, String url, Integer nChapter, String description, List<String> genres) {
        super(title, author, image, url, nChapter);
        this.description = description;
        this.genres = genres;
    }
}
