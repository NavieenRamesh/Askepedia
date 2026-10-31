package com.askepedia.service;

import com.askepedia.dao.ArticleDao;
import com.askepedia.dao.PassageDao;
import com.askepedia.dto.BatchIngestionResponse;
import com.askepedia.dto.IngestionResponse;
import com.askepedia.entity.Article;
import com.askepedia.entity.Passage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
class IngestionServiceTest {

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private PassageDao passageDao;

    @MockitoBean
    private WikipediaService wikipediaService;

    @Test
    @DisplayName("ingestArticle saves article and chunks passages into database")
    void ingestArticle_savesAndChunks() {
        String longContent = """
            Nuclear fusion is a reaction in which two or more atomic nuclei combine to form one or more different atomic nuclei.
            The difference in mass between the reactants and products is manifested as either the release or the absorption of energy.
            
            Fusion powers active stars such as the Sun. In the core of the Sun, huge temperatures and pressures fuse hydrogen into helium.
            """;

        Article mockArticle = Article.of(
                "Nuclear fusion",
                "https://en.wikipedia.org/wiki/Nuclear_fusion",
                longContent
        );

        given(wikipediaService.fetchFullArticle("Nuclear fusion")).willReturn(mockArticle);

        IngestionResponse response = ingestionService.ingestArticle("Nuclear fusion");

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.passagesCreated()).isGreaterThanOrEqualTo(1);

        Optional<Article> savedArticle = articleDao.findByTitle("Nuclear fusion");
        assertThat(savedArticle).isPresent();

        List<Passage> savedPassages = passageDao.findByArticleId(savedArticle.get().articleId());
        assertThat(savedPassages).hasSize(response.passagesCreated());
    }

    @Test
    @DisplayName("re-ingesting an existing article updates content without creating duplicate records (Idempotency)")
    void ingestArticle_idempotency_noDuplicates() {
        String initialContent = "First version content of the renewable energy topic.\n\nSecond paragraph details.";
        Article v1 = Article.of("Renewable energy", "https://wiki/Renewable_energy", initialContent);

        String updatedContent = "Updated newer version content of renewable energy.\n\nUpdated second paragraph.\n\nThird paragraph added.";
        Article v2 = Article.of("Renewable energy", "https://wiki/Renewable_energy", updatedContent);

        given(wikipediaService.fetchFullArticle("Renewable energy")).willReturn(v1, v2);

        // First ingestion
        IngestionResponse res1 = ingestionService.ingestArticle("Renewable energy");
        long initialArticleCount = articleDao.count();

        // Second ingestion (same title, updated text)
        IngestionResponse res2 = ingestionService.ingestArticle("Renewable energy");

        // Assert article count did not increase (no duplicate articles)
        assertThat(articleDao.count()).isEqualTo(initialArticleCount);
        assertThat(res1.articleId()).isEqualTo(res2.articleId());

        // Assert passages were cleanly refreshed
        List<Passage> passages = passageDao.findByArticleId(res2.articleId());
        assertThat(passages).hasSize(res2.passagesCreated());
    }

    @Test
    @DisplayName("ingestBatch handles multiple titles and reports summary")
    void ingestBatch_multipleTitles() {
        Article a1 = Article.of("Wind turbine", "https://wiki/Wind_turbine", "Wind power generates electricity.\n\nTurbines capture kinetic energy.");
        Article a2 = Article.of("Hydropower", "https://wiki/Hydropower", "Hydropower is energy from moving water.\n\nDams create water reservoirs.");

        given(wikipediaService.fetchFullArticle("Wind turbine")).willReturn(a1);
        given(wikipediaService.fetchFullArticle("Hydropower")).willReturn(a2);

        BatchIngestionResponse batchRes = ingestionService.ingestBatch(List.of("Wind turbine", "Hydropower"));

        assertThat(batchRes.totalRequested()).isEqualTo(2);
        assertThat(batchRes.totalSuccessful()).isEqualTo(2);
        assertThat(batchRes.totalFailed()).isEqualTo(0);
        assertThat(batchRes.totalPassagesCreated()).isGreaterThan(0);
    }
}
