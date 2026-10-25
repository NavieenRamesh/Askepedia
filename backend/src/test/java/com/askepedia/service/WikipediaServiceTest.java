package com.askepedia.service;

import com.askepedia.dto.ArticleResponse;
import com.askepedia.exception.ArticleNotFoundException;
import com.askepedia.exception.WikipediaApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WikipediaServiceTest {

    private WikipediaService wikipediaService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://en.wikipedia.org/api/rest_v1");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        wikipediaService = new WikipediaService(builder.build());
    }

    @Test
    @DisplayName("fetchArticle returns mapped ArticleResponse on success")
    void fetchArticle_success() {
        String mockJson = """
            {
                "pageid": 26751,
                "title": "Solar panel",
                "extract": "A solar panel is a device that converts sunlight into electricity.",
                "description": "Device that converts light into electricity",
                "content_urls": {
                    "desktop": {
                        "page": "https://en.wikipedia.org/wiki/Solar_panel"
                    }
                },
                "thumbnail": {
                    "source": "https://upload.wikimedia.org/solar.jpg"
                }
            }
            """;

        mockServer.expect(requestTo("https://en.wikipedia.org/api/rest_v1/page/summary/Solar_panel"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockJson, MediaType.APPLICATION_JSON));

        ArticleResponse response = wikipediaService.fetchArticle("Solar panel");

        assertThat(response).isNotNull();
        assertThat(response.pageId()).isEqualTo(26751L);
        assertThat(response.title()).isEqualTo("Solar panel");
        assertThat(response.extract()).isEqualTo("A solar panel is a device that converts sunlight into electricity.");
        assertThat(response.sourceUrl()).isEqualTo("https://en.wikipedia.org/wiki/Solar_panel");
        assertThat(response.thumbnailUrl()).isEqualTo("https://upload.wikimedia.org/solar.jpg");

        mockServer.verify();
    }

    @Test
    @DisplayName("fetchArticle throws ArticleNotFoundException when Wikipedia returns 404")
    void fetchArticle_notFound() {
        mockServer.expect(requestTo("https://en.wikipedia.org/api/rest_v1/page/summary/Unknown_Topic"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> wikipediaService.fetchArticle("Unknown Topic"))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessageContaining("Unknown Topic");

        mockServer.verify();
    }

    @Test
    @DisplayName("fetchArticle throws WikipediaApiException when Wikipedia returns 500 server error")
    void fetchArticle_serverError() {
        mockServer.expect(requestTo("https://en.wikipedia.org/api/rest_v1/page/summary/Error_Topic"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> wikipediaService.fetchArticle("Error Topic"))
                .isInstanceOf(WikipediaApiException.class);

        mockServer.verify();
    }

    @Test
    @DisplayName("fetchArticle throws IllegalArgumentException when title is blank")
    void fetchArticle_blankTitle() {
        assertThatThrownBy(() -> wikipediaService.fetchArticle("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }
}
