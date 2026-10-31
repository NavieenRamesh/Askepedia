package com.askepedia.controller;

import com.askepedia.dto.SearchResponse;
import com.askepedia.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing ranked full-text keyword search across stored Wikipedia passages.
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Executes keyword search against passages.
     * Example: GET /api/search?q=photovoltaic+cells&limit=10
     *
     * @param q search query string
     * @param limit optional result limit (default 10, max 50)
     * @return 200 OK with ranked SearchResponse
     */
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "limit", required = false) Integer limit) {
        SearchResponse response = searchService.search(q, limit);
        return ResponseEntity.ok(response);
    }
}
