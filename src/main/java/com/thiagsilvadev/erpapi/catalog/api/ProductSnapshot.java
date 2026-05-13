package com.thiagsilvadev.erpapi.catalog.api;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSnapshot(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal basePrice,
        ProductStatus status,
        CategorySnapshot category) {}
