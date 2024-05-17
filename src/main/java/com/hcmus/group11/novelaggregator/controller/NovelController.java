package com.hcmus.group11.novelaggregator.controller;

import com.hcmus.group11.novelaggregator.service.NovelService;
import com.hcmus.group11.novelaggregator.type.ChapterInfo;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.PluginMetadata;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
public class NovelController {
    private NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @Operation(summary = "Search novels by keyword")
    @GetMapping("/{pluginName}/search")
    public List<NovelSearchResult> search(@RequestParam() String q, @RequestParam(required = false) Integer page, @PathVariable() String pluginName) {
        if (page == null) {
            page = 0;
        }
        return novelService.search(q, page, pluginName);
    }

    @Operation(summary = "Get novel detail")
    @GetMapping("/{pluginName}/novel-detail")
    public NovelDetail getNovelDetail(@RequestParam() String url, @PathVariable() String pluginName) {
        return novelService.getNovelDetail(url, pluginName);
    }

    @Operation(summary = "Get chapter list")
    @GetMapping("/{pluginName}/chapter-list")
    public List<ChapterInfo> getChapterList(@RequestParam() String url, @RequestParam(required = false) Integer page, @PathVariable() String pluginName) {
        if (page == null) {
            page = 1;
        }
        return novelService.getChapterList(url, page, pluginName);
    }

    @Operation(summary = "Get plugin list")
    @GetMapping("/plugin-list")
    public List<PluginMetadata> getPluginList() {
        return novelService.getPluginList();
    }
}
