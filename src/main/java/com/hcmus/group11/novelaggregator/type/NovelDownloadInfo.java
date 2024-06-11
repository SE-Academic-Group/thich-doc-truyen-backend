package com.hcmus.group11.novelaggregator.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class NovelDownloadInfo {
    private String html;
    private Object metadata;
    private String chapterName;
}
