package com.askepedia.service;

import com.askepedia.dao.PassageDao;
import com.askepedia.dto.SearchResponse;
import com.askepedia.dto.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 50;

    private final PassageDao passageDao;

    public SearchService(PassageDao passageDao) {
        this.passageDao = passageDao;
    }

    /**
     * Executes keyword full-text search against stored Wikipedia passages.
     *
     * @param query natural language or keyword search query
     * @param limit requested maximum results (capped between 1 and MAX_LIMIT)
     * @return SearchResponse containing ranked results
     */
    public SearchResponse search(String query, Integer limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        int resolvedLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        String trimmedQuery = query.trim();

        log.debug("Executing full-text search for query: '{}' with limit: {}", trimmedQuery, resolvedLimit);

        List<SearchResult> results = passageDao.searchPassages(trimmedQuery, resolvedLimit);

        log.debug("Full-text search returned {} matches for '{}'", results.size(), trimmedQuery);

        return SearchResponse.of(trimmedQuery, results);
    }
}
