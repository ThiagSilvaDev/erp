package com.thiagsilvadev.erpapi.catalog.api;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.model.Money;

public record ProductSnapshot(
        UUID id,
        String sku,
        String name,
        String description,
        Money basePrice,
        ProductStatus status,
        CategorySnapshot category) {}
