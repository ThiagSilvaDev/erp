package com.thiagsilvadev.erpapi.catalog.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        MoneyResponse basePrice,
        ProductStatus status,
        CategoryResponse category) {}
