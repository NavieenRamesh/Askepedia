package com.askepedia.controller;

import com.askepedia.dto.BatchIngestionRequest;
import com.askepedia.dto.BatchIngestionResponse;
import com.askepedia.dto.IngestionRequest;
import com.askepedia.dto.IngestionResponse;
import com.askepedia.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for importing Wikipedia articles into the local database
 * and chunking them into searchable evidence passages.
 */
@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Ingests a single article by title.
     * Example payload: { "title": "Solar panel" }
     */
    @PostMapping("/articles")
    public ResponseEntity<IngestionResponse> ingestArticle(@Valid @RequestBody IngestionRequest request) {
        IngestionResponse response = ingestionService.ingestArticle(request.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Ingests a list of articles in batch.
     * Example payload: { "titles": ["Solar panel", "Photovoltaics", "Renewable energy"] }
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchIngestionResponse> ingestBatch(@Valid @RequestBody BatchIngestionRequest request) {
        BatchIngestionResponse response = ingestionService.ingestBatch(request.titles());
        return ResponseEntity.ok(response);
    }
}
