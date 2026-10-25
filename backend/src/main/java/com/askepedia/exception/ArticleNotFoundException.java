package com.askepedia.exception;

/**
 * Thrown when an article title cannot be found on Wikipedia (HTTP 404).
 */
public class ArticleNotFoundException extends RuntimeException {

    private final String title;

    public ArticleNotFoundException(String title) {
        super("Wikipedia article not found: '" + title + "'");
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
