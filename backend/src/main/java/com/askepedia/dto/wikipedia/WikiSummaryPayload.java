package com.askepedia.dto.wikipedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps the raw JSON payload returned by Wikipedia's Page Summary REST API:
 * GET https://en.wikipedia.org/api/rest_v1/page/summary/{title}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WikiSummaryPayload(
    Long pageid,
    String title,
    String extract,
    String description,
    @JsonProperty("content_urls") ContentUrls contentUrls,
    Thumbnail thumbnail
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentUrls(
        Desktop desktop
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Desktop(
        String page
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnail(
        String source
    ) {}
}
