package com.hcmus.group11.novelaggregator.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(name = "DownloadOptions", description = "Information for downloading novel")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class DownloadOptions {
    @Schema(description = "Type of download", example = "pdf")
    private String type;
    @Schema(description = "Path to download", example = "/htmlToPdf")
    private String path;
    @Schema(description = "Description of download", example = "Download as PDF")
    private String description;
}
