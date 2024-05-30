package com.hcmus.group11.novelaggregator.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Details of a novel search result")
public class NovelSearchResult {

    @Schema(description = "Title of the novel", example = "Example Title")
    private String title;

    @Schema(description = "Author of the novel", example = "John Doe")
    private String author;

    @Schema(description = "URL of the novel's cover image", example = "http://example.com/image.jpg")
    private String image;

    @Schema(description = "URL of the novel detail", example = "http://example.com/novel")
    private String url;

    @Schema(description = "Number of chapters", example = "100")
    private Integer nChapter;
}
