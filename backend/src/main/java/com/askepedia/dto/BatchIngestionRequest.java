package com.askepedia.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchIngestionRequest(
    @NotEmpty(message = "Article titles list must not be empty")
    List<String> titles
) {}
