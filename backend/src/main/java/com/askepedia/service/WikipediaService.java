package com.askepedia.service;

import com.askepedia.dto.ArticleResponse;
import com.askepedia.dto.wikipedia.WikiSummaryPayload;
import com.askepedia.entity.Article;
import com.askepedia.exception.ArticleNotFoundException;
import com.askepedia.exception.WikipediaApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Service
public class WikipediaService {

    private final RestClient restClient;

    public WikipediaService(RestClient wikipediaRestClient) {
        this.restClient = wikipediaRestClient;
    }

    /**
     * Fetches a concise Wikipedia article summary by title.
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

    /**
     * Fetches the complete plain-text article from Wikipedia's Action Query API.
     * Provides rich multi-section content without HTML tags for database indexing and passage splitting.
     *
     * @param title Wikipedia article title
     * @return Article entity ready for database persistence
     * @throws ArticleNotFoundException if the article does not exist
     */
    public Article fetchFullArticle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Article title must not be blank");
        }

        String encodedTitle = URLEncoder.encode(title.trim(), StandardCharsets.UTF_8);
        URI uri = URI.create("https://en.wikipedia.org/w/api.php?action=query&prop=extracts%7Cinfo&inprop=url&explaintext=true&format=json&titles=" + encodedTitle);

        JsonNode rootNode = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new WikipediaApiException(
                            "Wikipedia Query API returned error: " + response.getStatusCode(),
                            response.getStatusCode().value()
                    );
                })
                .body(JsonNode.class);

        if (rootNode == null) {
            throw new WikipediaApiException("Empty response received from Wikipedia Query API", 500);
        }

        JsonNode pagesNode = rootNode.path("query").path("pages");
        if (pagesNode.isMissingNode() || pagesNode.isEmpty() || pagesNode.has("-1")) {
            throw new ArticleNotFoundException(title);
        }

        Iterator<JsonNode> elements = pagesNode.elements();
        if (!elements.hasNext()) {
            throw new ArticleNotFoundException(title);
        }

        JsonNode pageNode = elements.next();
        if (pageNode.has("missing")) {
            throw new ArticleNotFoundException(title);
        }

        String officialTitle = pageNode.path("title").asText(title.trim());
        String fullUrl = pageNode.path("canonicalurl").asText(pageNode.path("fullurl").asText("https://en.wikipedia.org/wiki/" + officialTitle.replace(' ', '_')));
        String content = pageNode.path("extract").asText("");

        // If extract is unexpectedly empty, fallback to summary extract
        if (content.isBlank()) {
            ArticleResponse summary = fetchArticle(title);
            content = summary.extract();
            fullUrl = summary.sourceUrl();
        }

        return Article.of(officialTitle, fullUrl, content);
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
