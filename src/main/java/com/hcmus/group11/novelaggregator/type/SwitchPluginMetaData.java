package com.hcmus.group11.novelaggregator.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SwitchPluginMetaData {
    PluginMetadata pluginMetadata;
    NovelSearchResult novelSearchResult;
    ChapterInfo chapterInfo;
}
