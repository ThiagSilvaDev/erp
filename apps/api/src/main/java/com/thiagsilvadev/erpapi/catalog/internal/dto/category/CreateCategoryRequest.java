package com.thiagsilvadev.erpapi.catalog.internal.dto.category;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCategoryRequest(
        @NotBlank @Schema(description = "Category code, trimmed and normalized to uppercase", example = "BEVERAGES")
                String code,
        @NotBlank String name,
        String description) {}
