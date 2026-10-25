package com.askepedia.service;

import com.askepedia.dto.ArticleResponse;
import com.askepedia.dto.wikipedia.WikiSummaryPayload;
import com.askepedia.exception.ArticleNotFoundException;
import com.askepedia.exception.WikipediaApiException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WikipediaService {

    private final RestClient restClient;

    public WikipediaService(RestClient wikipediaRestClient) {
        this.restClient = wikipediaRestClient;
    }

    /**
     * Fetches a Wikipedia article summary by title.
     *
     * @param title the title of the Wikipedia article (e.g. "Solar panel")
     * @return structured ArticleResponse
     * @throws ArticleNotFoundException if Wikipedia returns 404
     * @throws WikipediaApiException if upstream network or server error occurs
     */
    public ArticleResponse fetchArticle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Article title must not be blank");
        }

        // Wikipedia uses underscores for spaces in REST URLs (e.g. Solar_panel)
        String normalizedTitle = title.trim().replace(' ', '_');

        WikiSummaryPayload payload = restClient.get()
                .uri("/page/summary/{title}", normalizedTitle)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ArticleNotFoundException(title);
                })
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new WikipediaApiException(
                            "Wikipedia API returned error status: " + response.getStatusCode(),
                            response.getStatusCode().value()
                    );
                })
                .body(WikiSummaryPayload.class);

        if (payload == null) {
            throw new WikipediaApiException("Empty response received from Wikipedia API", 500);
        }

        return mapToResponse(payload);
    }

    private ArticleResponse mapToResponse(WikiSummaryPayload payload) {
        String sourceUrl = (payload.contentUrls() != null && payload.contentUrls().desktop() != null)
                ? payload.contentUrls().desktop().page()
                : null;

        String thumbnailUrl = (payload.thumbnail() != null)
                ? payload.thumbnail().source()
                : null;

        return new ArticleResponse(
                payload.pageid(),
                payload.title(),
                payload.extract(),
                sourceUrl,
                payload.description(),
                thumbnailUrl
        );
    }
}
