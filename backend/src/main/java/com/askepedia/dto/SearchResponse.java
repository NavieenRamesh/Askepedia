package com.askepedia.dto;

import java.util.List;

/**
 * Standard response payload returned for search queries.
 */
public record SearchResponse(
    String query,
    int totalResults,
    List<SearchResult> results
) {
    public static SearchResponse of(String query, List<SearchResult> results) {
        return new SearchResponse(query, results.size(), results);
    }
}
