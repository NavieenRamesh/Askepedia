package com.askepedia.service;

import com.askepedia.dao.ArticleDao;
import com.askepedia.dao.PassageDao;
import com.askepedia.dto.BatchIngestionResponse;
import com.askepedia.dto.IngestionResponse;
import com.askepedia.entity.Article;
import com.askepedia.entity.Passage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final WikipediaService wikipediaService;
    private final ArticleDao articleDao;
    private final PassageDao passageDao;
    private final PassageSplitter passageSplitter;

    public IngestionService(
            WikipediaService wikipediaService,
            ArticleDao articleDao,
            PassageDao passageDao,
            PassageSplitter passageSplitter) {
        this.wikipediaService = wikipediaService;
        this.articleDao = articleDao;
        this.passageDao = passageDao;
        this.passageSplitter = passageSplitter;
    }

    /**
     * Ingests a single Wikipedia article by title idempotently.
     * Fetches full article content, persists article metadata, splits into passages,
     * and refreshes stored passages without duplicate records.
     *
     * @param title Wikipedia article title (e.g. "Solar panel")
     * @return IngestionResponse summary
     */
    @Transactional
    public IngestionResponse ingestArticle(String title) {
        log.info("Starting ingestion for article: '{}'", title);

        // 1. Fetch full article from Wikipedia API
        Article fetchedArticle = wikipediaService.fetchFullArticle(title);

        // 2. Persist or update article in database (idempotent upsert)
        Long articleId = articleDao.save(fetchedArticle);

        // 3. Clear any existing passages for this article to ensure clean refresh
        passageDao.deleteByArticleId(articleId);

        // 4. Split full content into evidence passages
        List<Passage> passages = passageSplitter.split(articleId, fetchedArticle.content());

        // 5. Batch insert passages
        if (!passages.isEmpty()) {
            passageDao.saveAll(passages);
        }

        log.info("Successfully ingested article '{}' (id: {}) with {} passages",
                fetchedArticle.title(), articleId, passages.size());

        return IngestionResponse.success(articleId, fetchedArticle.title(), fetchedArticle.sourceUrl(), passages.size());
    }

    /**
     * Ingests a batch of Wikipedia articles.
     */
    public BatchIngestionResponse ingestBatch(List<String> titles) {
        log.info("Starting batch ingestion of {} articles", titles.size());

        List<IngestionResponse> results = new ArrayList<>();
        int successful = 0;
        int failed = 0;
        int totalPassages = 0;

        for (String title : titles) {
            try {
                IngestionResponse response = ingestArticle(title);
                results.add(response);
                successful++;
                totalPassages += response.passagesCreated();
            } catch (Exception e) {
                log.error("Failed to ingest article '{}': {}", title, e.getMessage());
                results.add(IngestionResponse.failed(title, e.getMessage()));
                failed++;
            }
        }

        return new BatchIngestionResponse(
                titles.size(),
                successful,
                failed,
                totalPassages,
                results
        );
    }
}
