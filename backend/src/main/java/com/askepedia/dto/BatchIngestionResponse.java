package com.askepedia.dto;

import java.util.List;

public record BatchIngestionResponse(
    int totalRequested,
    int totalSuccessful,
    int totalFailed,
    int totalPassagesCreated,
    List<IngestionResponse> results
) {}
