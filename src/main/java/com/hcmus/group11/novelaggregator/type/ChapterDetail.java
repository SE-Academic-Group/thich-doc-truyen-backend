package com.hcmus.group11.novelaggregator.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details of a chapter")
public class ChapterDetail {

    @Schema(description = "Title of the novel", example = "ABC Novel")
    private String novelTitle;

    @Schema(description = "Title of the chapter", example = "Chapter 1: ABCXYZ")
    private String title;

    @Schema(description = "URL of the chapter detail", example = "http://example.com/chapter/1")
    private String url;

    @Schema(description = "Content of the chapter", example = "This is the content of the chapter")
    private String content;
}
