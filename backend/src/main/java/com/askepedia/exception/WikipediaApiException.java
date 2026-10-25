package com.askepedia.exception;

/**
 * Thrown when an unexpected error occurs while calling the Wikipedia API (HTTP 5xx, network timeouts).
 */
public class WikipediaApiException extends RuntimeException {

    private final int statusCode;

    public WikipediaApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public WikipediaApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
