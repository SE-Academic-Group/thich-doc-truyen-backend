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
@Schema(description = "Information of a chapter")
public class ChapterInfo {

    @Schema(description = "Title of the chapter", example = "Chapter 1: ABC")
    private String title;

    @Schema(description = "URL of the chapter", example = "http://example.com/chapter/1")
    private String url;

    @Schema(description = "Index of the chapter", example = "1")
    private String index;
}
