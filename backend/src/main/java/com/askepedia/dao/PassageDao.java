package com.askepedia.dao;

import com.askepedia.entity.Passage;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for persisting and querying 'wiki_passage' records.
 */
@Repository
public class PassageDao {

    private final JdbcClient jdbcClient;

    private static final RowMapper<Passage> PASSAGE_ROW_MAPPER = (rs, rowNum) -> new Passage(
            rs.getLong("passage_id"),
            rs.getLong("article_id"),
            rs.getInt("sequence_no"),
            rs.getString("passage_text"),
            rs.getTimestamp("created_at").toInstant()
    );

    public PassageDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Saves a passage idempotently.
     * If a passage with the same articleId and sequenceNo exists, updates its text.
     *
     * @param passage the Passage to save
     * @return the assigned or existing passageId
     */
    public Long save(Passage passage) {
        String sql = """
            INSERT INTO wiki_passage (article_id, sequence_no, passage_text, created_at)
            VALUES (:articleId, :sequenceNo, :passageText, :createdAt)
            ON CONFLICT (article_id, sequence_no) DO UPDATE
            SET passage_text = EXCLUDED.passage_text
            RETURNING passage_id
            """;

        return jdbcClient.sql(sql)
                .param("articleId", passage.articleId())
                .param("sequenceNo", passage.sequenceNo())
                .param("passageText", passage.passageText())
                .param("createdAt", Timestamp.from(passage.createdAt()))
                .query(Long.class)
                .single();
    }

    /**
     * Saves a batch of passages in a single transaction.
     *
     * @param passages list of passages to save
     * @return list of assigned passage IDs
     */
    @Transactional
    public List<Long> saveAll(List<Passage> passages) {
        List<Long> ids = new ArrayList<>(passages.size());
        for (Passage passage : passages) {
            ids.add(save(passage));
        }
        return ids;
    }

    public List<Passage> findByArticleId(Long articleId) {
        String sql = """
            SELECT passage_id, article_id, sequence_no, passage_text, created_at
            FROM wiki_passage
            WHERE article_id = :articleId
            ORDER BY sequence_no ASC
            """;

        return jdbcClient.sql(sql)
                .param("articleId", articleId)
                .query(PASSAGE_ROW_MAPPER)
                .list();
    }

    public Optional<Passage> findByArticleIdAndSequenceNo(Long articleId, int sequenceNo) {
        String sql = """
            SELECT passage_id, article_id, sequence_no, passage_text, created_at
            FROM wiki_passage
            WHERE article_id = :articleId AND sequence_no = :sequenceNo
            """;

        return jdbcClient.sql(sql)
                .param("articleId", articleId)
                .param("sequenceNo", sequenceNo)
                .query(PASSAGE_ROW_MAPPER)
                .optional();
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM wiki_passage";
        return jdbcClient.sql(sql)
                .query(Long.class)
                .single();
    }

    public int deleteByArticleId(Long articleId) {
        String sql = "DELETE FROM wiki_passage WHERE article_id = :articleId";
        return jdbcClient.sql(sql)
                .param("articleId", articleId)
                .update();
    }
}
