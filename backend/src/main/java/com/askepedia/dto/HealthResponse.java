package com.askepedia.dto;

import java.time.Instant;

/**
 * Data Transfer Object representing the system health status.
 */
public record HealthResponse(
    String status,
    String service,
    String version,
    Instant timestamp
) {}
