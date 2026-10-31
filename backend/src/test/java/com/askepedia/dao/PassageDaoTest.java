package com.askepedia.dao;

import com.askepedia.entity.Article;
import com.askepedia.entity.Passage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PassageDaoTest {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private PassageDao passageDao;

    private Long articleId;

    @BeforeEach
    void setUp() {
        Article article = Article.of(
                "Photosynthesis",
                "https://en.wikipedia.org/wiki/Photosynthesis",
                "Photosynthesis is a biological process used by plants."
        );
        articleId = articleDao.save(article);
    }

    @Test
    @DisplayName("saveAll persists a batch of passages and findByArticleId returns them sorted by sequence_no")
    void saveAllAndFindByArticleId() {
        Passage p1 = Passage.of(articleId, 1, "First paragraph about light-dependent reactions.");
        Passage p2 = Passage.of(articleId, 2, "Second paragraph about Calvin cycle.");
        Passage p3 = Passage.of(articleId, 3, "Third paragraph about chloroplasts.");

        List<Long> ids = passageDao.saveAll(List.of(p1, p2, p3));
        assertThat(ids).hasSize(3);

        List<Passage> passages = passageDao.findByArticleId(articleId);
        assertThat(passages).hasSize(3);
        assertThat(passages.get(0).sequenceNo()).isEqualTo(1);
        assertThat(passages.get(1).sequenceNo()).isEqualTo(2);
        assertThat(passages.get(2).sequenceNo()).isEqualTo(3);
        assertThat(passages.get(0).passageText()).contains("light-dependent");
    }

    @Test
    @DisplayName("save with duplicate (articleId, sequenceNo) updates existing text without duplicate rows")
    void saveDuplicateSequenceNoUpdatesText() {
        Passage p1 = Passage.of(articleId, 1, "Original passage text.");
        Long id1 = passageDao.save(p1);

        Passage p1Updated = Passage.of(articleId, 1, "Updated passage text.");
        Long id2 = passageDao.save(p1Updated);

        assertThat(id1).isEqualTo(id2);

        Optional<Passage> retrieved = passageDao.findByArticleIdAndSequenceNo(articleId, 1);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().passageText()).isEqualTo("Updated passage text.");
    }

    @Test
    @DisplayName("deleting parent article cascades and deletes all associated passages")
    void deletingArticleCascadesToPassages() {
        passageDao.save(Passage.of(articleId, 1, "Passage 1"));
        passageDao.save(Passage.of(articleId, 2, "Passage 2"));

        assertThat(passageDao.findByArticleId(articleId)).hasSize(2);

        // Delete parent article
        articleDao.deleteById(articleId);

        // Passages should be automatically cascade-deleted by foreign key constraint
        assertThat(passageDao.findByArticleId(articleId)).isEmpty();
    }
}
