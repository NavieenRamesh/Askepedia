package com.askepedia.controller;

import com.askepedia.dto.ArticleResponse;
import com.askepedia.service.WikipediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller providing endpoints to search and fetch Wikipedia articles.
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final WikipediaService wikipediaService;

    public ArticleController(WikipediaService wikipediaService) {
        this.wikipediaService = wikipediaService;
    }

    /**
     * Fetches summary and metadata for a given article title.
     * Example: GET /api/articles?title=Solar_panel
     *
     * @param title the article title query parameter
     * @return 200 OK with ArticleResponse
     */
    @GetMapping
    public ResponseEntity<ArticleResponse> getArticle(@RequestParam(name = "title") String title) {
        ArticleResponse response = wikipediaService.fetchArticle(title);
        return ResponseEntity.ok(response);
    }
}
