package com.askepedia.controller;

import com.askepedia.dto.SearchResponse;
import com.askepedia.dto.SearchResult;
import com.askepedia.service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @Test
    @DisplayName("GET /api/search?q=solar returns 200 with SearchResponse")
    void search_success() throws Exception {
        SearchResult result = new SearchResult(
                1L,
                10L,
                "Solar panel",
                "https://en.wikipedia.org/wiki/Solar_panel",
                1,
                "A solar panel converts sunlight into electricity.",
                0.85
        );

        SearchResponse response = SearchResponse.of("solar", List.of(result));
        given(searchService.search("solar", null)).willReturn(response);

        mockMvc.perform(get("/api/search")
                        .param("q", "solar")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("solar"))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.results[0].passageId").value(1L))
                .andExpect(jsonPath("$.results[0].articleTitle").value("Solar panel"))
                .andExpect(jsonPath("$.results[0].score").value(0.85));
    }

    @Test
    @DisplayName("GET /api/search without q parameter returns 400 Bad Request")
    void search_missingQuery() throws Exception {
        mockMvc.perform(get("/api/search")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
