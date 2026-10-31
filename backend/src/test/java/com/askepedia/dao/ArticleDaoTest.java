package com.askepedia.dao;

import com.askepedia.entity.Article;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ArticleDaoTest {

    @Autowired
    private ArticleDao articleDao;

    @Test
    @DisplayName("save and findById persists and retrieves an Article")
    void saveAndFindById() {
        Article article = Article.of(
                "Solar panel",
                "https://en.wikipedia.org/wiki/Solar_panel",
                "A solar panel converts sunlight into electricity."
        );

        Long id = articleDao.save(article);
        assertThat(id).isNotNull().isPositive();

        Optional<Article> retrieved = articleDao.findById(id);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().title()).isEqualTo("Solar panel");
        assertThat(retrieved.get().sourceUrl()).isEqualTo("https://en.wikipedia.org/wiki/Solar_panel");
        assertThat(retrieved.get().content()).isEqualTo("A solar panel converts sunlight into electricity.");
        assertThat(retrieved.get().fetchedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByTitle retrieves the article by unique title")
    void findByTitle() {
        Article article = Article.of(
                "Artificial intelligence",
                "https://en.wikipedia.org/wiki/Artificial_intelligence",
                "AI is intelligence demonstrated by machines."
        );
        articleDao.save(article);

        Optional<Article> retrieved = articleDao.findByTitle("Artificial intelligence");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().title()).isEqualTo("Artificial intelligence");
    }

    @Test
    @DisplayName("save with duplicate title updates content and does not create duplicate row")
    void saveDuplicateTitleUpdatesContent() {
        Article original = Article.of(
                "Quantum computing",
                "https://en.wikipedia.org/wiki/Quantum_computing",
                "Original content."
        );
        Long id1 = articleDao.save(original);

        Article updated = new Article(
                null,
                "Quantum computing",
                "https://en.wikipedia.org/wiki/Quantum_computing",
                "Updated quantum mechanics content.",
                Instant.now()
        );
        Long id2 = articleDao.save(updated);

        // Same ID returned, no duplicate row created
        assertThat(id1).isEqualTo(id2);

        Optional<Article> retrieved = articleDao.findById(id1);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().content()).isEqualTo("Updated quantum mechanics content.");
    }

    @Test
    @DisplayName("deleteById removes the article from the database")
    void deleteById() {
        Article article = Article.of(
                "Temporary article",
                "https://en.wikipedia.org/wiki/Temp",
                "Content to be deleted."
        );
        Long id = articleDao.save(article);
        assertThat(articleDao.findById(id)).isPresent();

        boolean deleted = articleDao.deleteById(id);
        assertThat(deleted).isTrue();
        assertThat(articleDao.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("findAll returns paginated articles ordered by ID")
    void findAll_pagination() {
        articleDao.save(Article.of("Article 1", "https://wiki/1", "Content 1"));
        articleDao.save(Article.of("Article 2", "https://wiki/2", "Content 2"));
        articleDao.save(Article.of("Article 3", "https://wiki/3", "Content 3"));

        List<Article> page = articleDao.findAll(2, 0);
        assertThat(page).hasSize(2);

        long count = articleDao.count();
        assertThat(count).isGreaterThanOrEqualTo(3L);
    }
}
