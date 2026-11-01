package com.thiagsilvadev.erpapi.catalog.internal.dto.product;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyRequest;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateProductRequest(
        @Schema(
                        description =
                                "Omit or set null to generate CATEGORY-00000001; supplied SKUs must be non-blank and at most 80 characters.",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                String sku,
        @NotBlank String name,
        String description,
        @Valid @NotNull MoneyRequest basePrice,
        @NotNull UUID categoryId) {}
