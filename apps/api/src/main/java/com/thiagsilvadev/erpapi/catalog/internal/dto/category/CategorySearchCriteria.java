package com.thiagsilvadev.erpapi.catalog.internal.dto.category;

import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategorySearchCriteria(
        @Size(max = 40) @Schema(description = "Category code, trimmed and normalized to uppercase", example = "BEVERAGES")
                String code,
        @Size(max = 120) String name,
        CategoryStatus status) {}
