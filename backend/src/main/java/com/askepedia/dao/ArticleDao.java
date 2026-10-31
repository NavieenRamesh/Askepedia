package com.askepedia.dao;

import com.askepedia.entity.Article;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for persisting and querying 'wiki_article' records.
 */
@Repository
public class ArticleDao {

    private final JdbcClient jdbcClient;

    private static final RowMapper<Article> ARTICLE_ROW_MAPPER = (rs, rowNum) -> new Article(
            rs.getLong("article_id"),
            rs.getString("title"),
            rs.getString("source_url"),
            rs.getString("content"),
            rs.getTimestamp("fetched_at").toInstant()
    );

    public ArticleDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Saves an article idempotently.
     * If an article with the same title already exists, updates its content and timestamp.
     *
     * @param article the Article to save
     * @return the assigned or existing articleId
     */
    public Long save(Article article) {
        String sql = """
            INSERT INTO wiki_article (title, source_url, content, fetched_at)
            VALUES (:title, :sourceUrl, :content, :fetchedAt)
            ON CONFLICT (title) DO UPDATE
            SET source_url = EXCLUDED.source_url,
                content = EXCLUDED.content,
                fetched_at = EXCLUDED.fetched_at
            RETURNING article_id
            """;

        return jdbcClient.sql(sql)
                .param("title", article.title())
                .param("sourceUrl", article.sourceUrl())
                .param("content", article.content())
                .param("fetchedAt", Timestamp.from(article.fetchedAt()))
                .query(Long.class)
                .single();
    }

    public Optional<Article> findById(Long articleId) {
        String sql = """
            SELECT article_id, title, source_url, content, fetched_at
            FROM wiki_article
            WHERE article_id = :id
            """;

        return jdbcClient.sql(sql)
                .param("id", articleId)
                .query(ARTICLE_ROW_MAPPER)
                .optional();
    }

    public Optional<Article> findByTitle(String title) {
        String sql = """
            SELECT article_id, title, source_url, content, fetched_at
            FROM wiki_article
            WHERE title = :title
            """;

        return jdbcClient.sql(sql)
                .param("title", title)
                .query(ARTICLE_ROW_MAPPER)
                .optional();
    }

    public List<Article> findAll(int limit, int offset) {
        String sql = """
            SELECT article_id, title, source_url, content, fetched_at
            FROM wiki_article
            ORDER BY article_id ASC
            LIMIT :limit OFFSET :offset
            """;

        return jdbcClient.sql(sql)
                .param("limit", limit)
                .param("offset", offset)
                .query(ARTICLE_ROW_MAPPER)
                .list();
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM wiki_article";
        return jdbcClient.sql(sql)
                .query(Long.class)
                .single();
    }

    public boolean deleteById(Long articleId) {
        String sql = "DELETE FROM wiki_article WHERE article_id = :id";
        int rowsAffected = jdbcClient.sql(sql)
                .param("id", articleId)
                .update();
        return rowsAffected > 0;
    }
}
