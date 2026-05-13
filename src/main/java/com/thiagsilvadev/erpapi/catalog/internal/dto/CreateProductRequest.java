package com.thiagsilvadev.erpapi.catalog.internal.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotNull @PositiveOrZero BigDecimal basePrice,
        @NotNull UUID categoryId) {}
