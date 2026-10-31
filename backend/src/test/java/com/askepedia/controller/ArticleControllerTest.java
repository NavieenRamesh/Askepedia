package com.askepedia.controller;

import com.askepedia.dto.ArticleResponse;
import com.askepedia.exception.ArticleNotFoundException;
import com.askepedia.service.WikipediaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WikipediaService wikipediaService;

    @Test
    @DisplayName("GET /api/articles?title=Solar_panel returns 200 with ArticleResponse")
    void getArticle_success() throws Exception {
        ArticleResponse mockArticle = new ArticleResponse(
                26751L,
                "Solar panel",
                "A solar panel converts sunlight into electricity.",
                "https://en.wikipedia.org/wiki/Solar_panel",
                "Device that converts light into electricity",
                "https://upload.wikimedia.org/solar.jpg"
        );

        given(wikipediaService.fetchArticle("Solar_panel")).willReturn(mockArticle);

        mockMvc.perform(get("/api/articles")
                        .param("title", "Solar_panel")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageId").value(26751L))
                .andExpect(jsonPath("$.title").value("Solar panel"))
                .andExpect(jsonPath("$.extract").value("A solar panel converts sunlight into electricity."))
                .andExpect(jsonPath("$.sourceUrl").value("https://en.wikipedia.org/wiki/Solar_panel"))
                .andExpect(jsonPath("$.thumbnailUrl").value("https://upload.wikimedia.org/solar.jpg"));
    }

    @Test
    @DisplayName("GET /api/articles with nonexistent title returns 404 Not Found")
    void getArticle_notFound() throws Exception {
        given(wikipediaService.fetchArticle("NonExistent"))
                .willThrow(new ArticleNotFoundException("NonExistent"));

        mockMvc.perform(get("/api/articles")
                        .param("title", "NonExistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Wikipedia article not found: 'NonExistent'"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/articles without title parameter returns 400 Bad Request")
    void getArticle_missingParameter() throws Exception {
        mockMvc.perform(get("/api/articles")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
