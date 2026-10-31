package com.askepedia.dao;

import com.askepedia.dto.SearchResult;
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

    private static final RowMapper<SearchResult> SEARCH_RESULT_ROW_MAPPER = (rs, rowNum) -> new SearchResult(
            rs.getLong("passage_id"),
            rs.getLong("article_id"),
            rs.getString("article_title"),
            rs.getString("source_url"),
            rs.getInt("sequence_no"),
            rs.getString("passage_text"),
            rs.getDouble("score")
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

    /**
     * Executes a full-text search across all stored passages using PostgreSQL's
     * websearch_to_tsquery and ts_rank algorithms, joining with wiki_article for source attribution.
     *
     * @param query search query
     * @param limit maximum passages to return
     * @return list of ranked SearchResult records
     */
    public List<SearchResult> searchPassages(String query, int limit) {
        String sql = """
            SELECT p.passage_id,
                   p.article_id,
                   a.title AS article_title,
                   a.source_url,
                   p.sequence_no,
                   p.passage_text,
                   ts_rank(p.search_vector, websearch_to_tsquery('english', :query)) AS score
            FROM wiki_passage p
            JOIN wiki_article a ON p.article_id = a.article_id
            WHERE p.search_vector @@ websearch_to_tsquery('english', :query)
            ORDER BY score DESC, p.sequence_no ASC
            LIMIT :limit
            """;

        return jdbcClient.sql(sql)
                .param("query", query)
                .param("limit", limit)
                .query(SEARCH_RESULT_ROW_MAPPER)
                .list();
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
