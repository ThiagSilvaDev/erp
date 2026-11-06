package com.thiagsilvadev.erpapi.catalog.internal.dto.product;

import java.util.UUID;

import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductSearchCriteria(
        @Size(max = 80) @Schema(
                        description = "Exact SKU filter, trimmed and normalized to uppercase",
                        example = "BEVERAGES-00000001")
                String sku,
        @Size(max = 120) String name,
        UUID categoryId,
        ProductStatus status) {}
