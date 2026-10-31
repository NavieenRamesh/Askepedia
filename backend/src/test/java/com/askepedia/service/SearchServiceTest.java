package com.askepedia.service;

import com.askepedia.dao.PassageDao;
import com.askepedia.dto.SearchResponse;
import com.askepedia.dto.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private PassageDao passageDao;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(passageDao);
    }

    @Test
    @DisplayName("search validates blank query and throws IllegalArgumentException")
    void search_blankQueryThrowsException() {
        assertThatThrownBy(() -> searchService.search("   ", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("search caps limit to MAX_LIMIT (50)")
    void search_capsMaxLimit() {
        given(passageDao.searchPassages("energy", 50)).willReturn(List.of());

        SearchResponse response = searchService.search("energy", 100);

        assertThat(response.query()).isEqualTo("energy");
        assertThat(response.totalResults()).isZero();
    }

    @Test
    @DisplayName("search delegates to passageDao and maps results")
    void search_delegatesAndMapsResults() {
        SearchResult result = new SearchResult(
                1L, 10L, "Solar energy", "https://wiki/solar", 1, "Photons hit cells.", 0.92
        );
        given(passageDao.searchPassages("photons", 10)).willReturn(List.of(result));

        SearchResponse response = searchService.search("photons", 10);

        assertThat(response.totalResults()).isEqualTo(1);
        assertThat(response.results().get(0).score()).isEqualTo(0.92);
        assertThat(response.results().get(0).articleTitle()).isEqualTo("Solar energy");
    }
}
