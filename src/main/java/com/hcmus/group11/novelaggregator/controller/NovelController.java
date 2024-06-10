package com.hcmus.group11.novelaggregator.controller;

import com.hcmus.group11.novelaggregator.exception.type.TransformedHttpException;
import com.hcmus.group11.novelaggregator.service.NovelService;
import com.hcmus.group11.novelaggregator.type.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
public class NovelController {

    private NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @Operation(summary = "Search novels by keyword",
            parameters = {
                    @Parameter(name = "q", description = "Keyword for searching novels", required = true, example = "Fantasy"),
                    @Parameter(name = "page", description = "Page number for pagination", required = false, example = "1"),
                    @Parameter(name = "pluginName", description = "Name of the plugin to use for search", required = true, example = "truyenFull")
            },
            responses = {
                    @ApiResponse(description = "List of novels matching the search criteria",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = NovelSearchResult.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid input",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/{pluginName}/search")
    public List<NovelSearchResult> search(@RequestParam() String q, @RequestParam(required = false) Integer page, @PathVariable() String pluginName) {
        if (page == null) {
            page = 1;
        }
        return novelService.search(q, page, pluginName);
    }

    @Operation(summary = "Get novel detail",
            parameters = {
                    @Parameter(name = "url", description = "URL of the novel", required = true, example = "http://example.com/novel")
            },
            responses = {
                    @ApiResponse(description = "Details of the novel",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NovelDetail.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid URL",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/novel-detail")
    public NovelDetail getNovelDetail(@RequestParam() String url) {
        return novelService.getNovelDetail(url);
    }

    @Operation(summary = "Get chapter list",
            parameters = {
                    @Parameter(name = "url", description = "URL of the novel", required = true, example = "http://example.com/novel"),
                    @Parameter(name = "page", description = "Page number for pagination", required = false, example = "1")
            },
            responses = {
                    @ApiResponse(description = "List of chapters",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ChapterInfo.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid URL or page number",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/chapter-list")
    public List<ChapterInfo> getChapterList(@RequestParam() String url, @RequestParam(required = false) Integer page) {
        if (page == null) {
            page = 1;
        }
        return novelService.getChapterList(url, page);
    }

    @Operation(summary = "Get plugin list",
            responses = {
                    @ApiResponse(description = "List of available plugins",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PluginMetadata.class)))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/plugin-list")
    public List<PluginMetadata> getPluginList() {
        return novelService.getPluginList();
    }

    @Operation(summary = "Get chapter detail",
            parameters = {
                    @Parameter(name = "novelUrl", description = "URL of the novel", required = true, example = "http://example.com/novel"),
                    @Parameter(name = "chapterUrl", description = "URL of the chapter", required = true, example = "http://example.com/chapter")
            },
            responses = {
                    @ApiResponse(description = "Details of the chapter",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChapterDetail.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid URL",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/chapter-detail")
    public Object getChapterDetail(@RequestParam() String chapterUrl, @RequestParam String novelUrl) {
        return novelService.getChapterDetail(chapterUrl);
    }

    @Operation(summary = "Get full chapter list",
            parameters = {
                    @Parameter(name = "url", description = "URL of the novel", required = true, example = "http://example.com/novel")
            },
            responses = {
                    @ApiResponse(description = "List of all chapters",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ChapterInfo.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid URL",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/full-chapter-list")
    public Object getFullChapterList(@RequestParam() String url) {
        return novelService.getFullChapterList(url);
    }

    @Operation(summary = "Switch plugin metadata",
            parameters = {
                    @Parameter(name = "noveUrl", description = "URL of the novel", required = true, example = "http://example.com/novel"),
                    @Parameter(name = "chapterIndex", description = "Index of the chapter", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(description = "Chapter Information of each plugin",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ChapterInfo.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid URL",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/switch-plugin")
    public List<SwitchPluginMetaData> getSwitchPluginMetaData(@RequestParam() String chapterIndex, @RequestParam String novelUrl) {
        return novelService.getSwitchPluginMetaData(chapterIndex, novelUrl);
    }

    @Operation(summary = "Get download options",
            responses = {
                    @ApiResponse(description = "list chapter download options",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DownloadOptions.class)))),
                    @ApiResponse(responseCode = "400", description = "No endpoint found for the request",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/download-options")
    public List<DownloadPluginMetadata> getDownloadOptions() {
        return novelService.getDownloadOptionsList();
    }

    @Operation(summary = "Download novel",
            parameters = {
                    @Parameter(name = "url", description = "URL of the novel", required = true, example = "http://example.com/novel"),
                    @Parameter(name = "type", description = "Type of download", required = true, example = "epub")
            },
            responses = {
                    @ApiResponse(description = "Downloaded file",
                            responseCode = "200",
                            content = @Content(mediaType = "application/octet-stream")),
                    @ApiResponse(responseCode = "400", description = "Invalid URL or type",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransformedHttpException.class)))
            }
    )
    @GetMapping("/download")
    public Object downloadNovel(@RequestParam() String url, @RequestParam() String type) throws Exception {
        return novelService.exportNovel(url, type);
    }
}
