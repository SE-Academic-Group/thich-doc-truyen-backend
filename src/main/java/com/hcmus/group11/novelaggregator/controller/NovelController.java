package com.hcmus.group11.novelaggregator.controller;

import com.hcmus.group11.novelaggregator.service.NovelService;
import com.hcmus.group11.novelaggregator.type.ChapterInfo;
import com.hcmus.group11.novelaggregator.type.NovelDetail;
import com.hcmus.group11.novelaggregator.type.NovelSearchResult;
import com.hcmus.group11.novelaggregator.type.PluginMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NovelController {
    private NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @GetMapping("/{pluginName}/search")
    public List<NovelSearchResult> search(@RequestParam() String q, @RequestParam(required = false) Integer page, @PathVariable() String pluginName) {
        if (page == null) {
            page = 1;
        }
        return novelService.search(q, page, pluginName);
    }

    @GetMapping("/{pluginName}/novel-detail")
    public NovelDetail getNovelDetail(@RequestParam() String url, @PathVariable() String pluginName) {
        return novelService.getNovelDetail(url, pluginName);
    }

    @GetMapping("/{pluginName}/chapter-list")
    public List<ChapterInfo> getChapterList(@RequestParam() String url, @RequestParam(required = false) Integer page, @PathVariable() String pluginName) {
        if (page == null) {
            page = 1;
        }
        return novelService.getChapterList(url, page, pluginName);
    }

    @GetMapping("/plugin-list")
    public List<PluginMetadata> getPluginList() {
        return novelService.getPluginList();
    }
}
