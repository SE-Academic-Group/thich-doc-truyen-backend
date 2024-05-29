package com.hcmus.group11.novelaggregator.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Metadata of a plugin")
public class PluginMetadata {
    @Schema(description = "Name of the plugin", example = "Example Plugin")
    private String name;

    @Schema(description = "URL of the plugin", example = "http://example.com/plugin")
    private String url;
}
