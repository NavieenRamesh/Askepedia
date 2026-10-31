package com.askepedia.dao;

import com.askepedia.dto.SearchResult;
import com.askepedia.entity.Article;
import com.askepedia.entity.Passage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PassageSearchIntegrationTest {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private PassageDao passageDao;

    private Long testArticleId;

    @BeforeEach
    void setUp() {
        Article solar = Article.of("Solar Technology Unique Test", "https://wiki/solar_test", "Full article content on solar panels.");
        testArticleId = articleDao.save(solar);

        passageDao.save(Passage.of(testArticleId, 1, "Photovoltaic cells directly convert sunlight into electrical energy using semiconductor materials."));
        passageDao.save(Passage.of(testArticleId, 2, "Crystalline semiconductor rooftop modules dominate modern installations."));
        passageDao.save(Passage.of(testArticleId, 3, "Wind turbines harness kinetic energy of atmospheric wind flow across turbine blades."));
    }

    @Test
    @DisplayName("searchPassages finds matching passages and ranks by relevance")
    void searchPassages_matchesAndRanks() {
        List<SearchResult> results = passageDao.searchPassages("photovoltaic semiconductor", 5);

        assertThat(results).isNotEmpty();
        SearchResult topResult = results.get(0);
        assertThat(topResult.passageText()).contains("Photovoltaic cells");
        assertThat(topResult.score()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("searchPassages supports websearch phrases with quotation marks")
    void searchPassages_phraseSearch() {
        List<SearchResult> results = passageDao.searchPassages("\"crystalline semiconductor rooftop modules\"", 5);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).passageText()).contains("Crystalline semiconductor rooftop modules");
    }

    @Test
    @DisplayName("searchPassages returns empty list when no matches are found")
    void searchPassages_noMatches() {
        List<SearchResult> results = passageDao.searchPassages("nonexistentxyzuniquetopic12345", 5);

        assertThat(results).isEmpty();
    }
}
