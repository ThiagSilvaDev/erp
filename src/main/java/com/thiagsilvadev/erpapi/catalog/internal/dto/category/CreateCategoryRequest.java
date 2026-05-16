package com.thiagsilvadev.erpapi.catalog.internal.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(@NotBlank String code, @NotBlank String name, String description) {}
