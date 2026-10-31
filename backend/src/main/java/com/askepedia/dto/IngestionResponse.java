package com.askepedia.dto;

public record IngestionResponse(
    Long articleId,
    String title,
    String sourceUrl,
    int passagesCreated,
    String status
) {
    public static IngestionResponse success(Long articleId, String title, String sourceUrl, int passagesCreated) {
        return new IngestionResponse(articleId, title, sourceUrl, passagesCreated, "SUCCESS");
    }

    public static IngestionResponse failed(String title, String error) {
        return new IngestionResponse(null, title, null, 0, "FAILED: " + error);
    }
}
