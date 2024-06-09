package com.hcmus.group11.novelaggregator.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SwitchPluginMetaData", description = "Switch plugin metadata")
public class SwitchPluginMetaData {
    @Schema(name = "pluginMetadata", description = "Plugin metadata")
    PluginMetadata pluginMetadata;
    @Schema(name = "novelSearchResult", description = "Novel search result")
    NovelSearchResult novelSearchResult;
    @Schema(name = "chapterInfo", description = "Chapter info")
    ChapterInfo chapterInfo;
}
