package com.askepedia.controller;

import com.askepedia.dto.BatchIngestionResponse;
import com.askepedia.dto.IngestionResponse;
import com.askepedia.service.IngestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngestionController.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestionService ingestionService;

    @Test
    @DisplayName("POST /api/ingestion/articles with valid title returns 201 Created and IngestionResponse")
    void ingestArticle_success() throws Exception {
        IngestionResponse mockResponse = IngestionResponse.success(
                1L,
                "Solar panel",
                "https://en.wikipedia.org/wiki/Solar_panel",
                12
        );

        given(ingestionService.ingestArticle("Solar panel")).willReturn(mockResponse);

        String requestBody = """
            {
                "title": "Solar panel"
            }
            """;

        mockMvc.perform(post("/api/ingestion/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.articleId").value(1L))
                .andExpect(jsonPath("$.title").value("Solar panel"))
                .andExpect(jsonPath("$.sourceUrl").value("https://en.wikipedia.org/wiki/Solar_panel"))
                .andExpect(jsonPath("$.passagesCreated").value(12))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /api/ingestion/articles with blank title returns 400 Bad Request")
    void ingestArticle_blankTitle() throws Exception {
        String requestBody = """
            {
                "title": "   "
            }
            """;

        mockMvc.perform(post("/api/ingestion/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/ingestion/batch imports multiple articles successfully")
    void ingestBatch_success() throws Exception {
        BatchIngestionResponse mockBatch = new BatchIngestionResponse(
                2,
                2,
                0,
                25,
                List.of(
                        IngestionResponse.success(1L, "Topic A", "https://wiki/A", 10),
                        IngestionResponse.success(2L, "Topic B", "https://wiki/B", 15)
                )
        );

        given(ingestionService.ingestBatch(List.of("Topic A", "Topic B"))).willReturn(mockBatch);

        String requestBody = """
            {
                "titles": ["Topic A", "Topic B"]
            }
            """;

        mockMvc.perform(post("/api/ingestion/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(2))
                .andExpect(jsonPath("$.totalSuccessful").value(2))
                .andExpect(jsonPath("$.totalPassagesCreated").value(25));
    }
}
