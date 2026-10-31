package com.askepedia.dto;

/**
 * Represents a single search result matching a user query,
 * containing the passage text, relevance score, and source article attribution.
 */
public record SearchResult(
    Long passageId,
    Long articleId,
    String articleTitle,
    String sourceUrl,
    int sequenceNo,
    String passageText,
    double score
) {}
