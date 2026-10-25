package com.askepedia.dto;

import java.time.Instant;

/**
 * Standard structured error payload returned for API exceptions.
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    Instant timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, Instant.now());
    }
}
