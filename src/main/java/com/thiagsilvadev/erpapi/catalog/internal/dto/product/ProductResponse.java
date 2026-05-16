package com.thiagsilvadev.erpapi.catalog.internal.dto.product;

import java.util.UUID;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyResponse;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        MoneyResponse basePrice,
        ProductStatus status,
        CategoryResponse category) {}
