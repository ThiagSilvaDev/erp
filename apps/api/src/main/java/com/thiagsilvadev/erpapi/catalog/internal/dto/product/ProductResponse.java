package com.thiagsilvadev.erpapi.catalog.internal.dto.product;

import java.util.UUID;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySummaryResponse;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        MoneyResponse basePrice,
        String formattedBasePrice,
        ProductStatus status,
        CategorySummaryResponse category) {}
