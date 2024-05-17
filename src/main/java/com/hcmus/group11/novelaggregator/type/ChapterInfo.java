package com.hcmus.group11.novelaggregator.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChapterInfo {
    private String title;
    private String url;
    private Integer index;
}
