package com.askepedia.dto;

/**
 * Public response DTO returned to Askepedia API consumers.
 */
public record ArticleResponse(
    Long pageId,
    String title,
    String extract,
    String sourceUrl,
    String description,
    String thumbnailUrl
) {}
