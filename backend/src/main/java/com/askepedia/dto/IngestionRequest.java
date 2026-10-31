package com.askepedia.dto;

import jakarta.validation.constraints.NotBlank;

public record IngestionRequest(
    @NotBlank(message = "Article title must not be blank")
    String title
) {}
