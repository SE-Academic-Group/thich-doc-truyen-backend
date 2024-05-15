package com.hcmus.group11.novelaggregator.controller;

import com.hcmus.group11.novelaggregator.plugin.PluginManager;
import com.hcmus.group11.novelaggregator.service.NovelService;
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

    @GetMapping("/search")
    public List<NovelSearchResult> search(@RequestParam("term") String param) {
        return novelService.search(param, "tangThuVien");
    }

    @GetMapping("/{pluginName}/search")
    public List<NovelSearchResult> search(@RequestParam("q") String param, @PathVariable() String pluginName) {
        return novelService.search(param, pluginName);
    }

    @GetMapping("/plugin-list")
    public List<PluginMetadata> getPluginList() {
        return novelService.getPluginList();
    }
}
