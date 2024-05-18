package com.hcmus.group11.novelaggregator.controller;

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
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NovelSearchResult.class)))),
                    @ApiResponse(description = "Invalid input", responseCode = "400",
                            content = @Content(schema = @Schema(example = "null")))
            }
    )
    @GetMapping("/{pluginName}/search")
    public List<NovelSearchResult> search(@RequestParam() String q, @RequestParam(required = false) Integer page, @PathVariable() String pluginName) {
        if (page == null) {
            page = 0;
        }
        return novelService.search(q, page, pluginName);
    }

    @Operation(summary = "Get novel detail",
            parameters = {
                    @Parameter(name = "url", description = "URL of the novel", required = true, example = "http://example.com/novel"),
                    @Parameter(name = "pluginName", description = "Name of the plugin to use for getting novel detail", required = true, example = "truyenFull")
            },
            responses = {
                    @ApiResponse(description = "Details of the novel",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NovelDetail.class))),
                    @ApiResponse(description = "Invalid URL", responseCode = "400",
                            content = @Content(schema = @Schema(example = "null")))
            }
    )
    @GetMapping("/{pluginName}/novel-detail")
    public NovelDetail getNovelDetail(@RequestParam() String url, @PathVariable() String pluginName) {
        return novelService.getNovelDetail(url, pluginName);
    }

    @Operation(summary = "Get chapter list",
            parameters = {
                    @Parameter(name = "url", description = "URL of the novel", required = true, example = "http://example.com/novel"),
                    @Parameter(name = "page", description = "Page number for pagination", required = false, example = "1"),
                    @Parameter(name = "pluginName", description = "Name of the plugin to use for getting chapter list", required = true, example = "truyenFull")
            },
            responses = {
                    @ApiResponse(description = "List of chapters",
                            responseCode = "200",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChapterInfo.class)))),
                    @ApiResponse(description = "Invalid URL or page number", responseCode = "400",
                            content = @Content(schema = @Schema(example = "null")))
            }
    )
    @GetMapping("/{pluginName}/chapter-list")
    public List<ChapterInfo> getChapterList(@RequestParam() String url, @RequestParam(required = false) Integer page, @PathVariable() String pluginName) {
        if (page == null) {
            page = 1;
        }
        return novelService.getChapterList(url, page, pluginName);
    }

    @Operation(summary = "Get plugin list",
            responses = {
                    @ApiResponse(description = "List of available plugins",
                            responseCode = "200",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PluginMetadata.class))))
            }
    )
    @GetMapping("/plugin-list")
    public List<PluginMetadata> getPluginList() {
        return novelService.getPluginList();
    }

    @Operation(summary = "Get chapter detail",
            parameters = {
                    @Parameter(name = "url", description = "URL of the chapter", required = true, example = "http://example.com/chapter"),
                    @Parameter(name = "pluginName", description = "Name of the plugin to use for getting chapter detail", required = true, example = "truyenFull")
            },
            responses = {
                    @ApiResponse(description = "Details of the chapter",
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ChapterDetail.class))),
                    @ApiResponse(description = "Invalid URL", responseCode = "400",
                            content = @Content(schema = @Schema(example = "null")))
            }
    )
    @GetMapping("/{pluginName}/chapter-detail")
    public Object getChapterDetail(@RequestParam() String url, @PathVariable() String pluginName) {
        return novelService.getChapterDetail(url, pluginName);
    }
}
