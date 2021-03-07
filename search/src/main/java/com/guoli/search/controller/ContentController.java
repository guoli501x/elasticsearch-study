package com.guoli.search.controller;

import com.guoli.search.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 * @author guoli
 * @data 2021-03-06 23:34
 */
@RestController
public class ContentController {
    private final ContentService contentService;

    @Autowired
    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping("/parseData")
    public String parseData(@RequestParam String keyword) {
        return contentService.parseDataToEs(keyword);
    }

    @GetMapping("/search/{keyword}/{from}/{size}")
    public List<Map<String, Object>> search(@PathVariable("keyword") String keyword, @PathVariable("from") int from,
                                            @PathVariable("size") int size) {
        return contentService.searchPage(keyword, from, size);
    }
}
