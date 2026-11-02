package com.thiagsilvadev.erpapi.catalog.internal.dto.product;

import java.util.UUID;

import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;

public record ProductSearchCriteria(
        @Size(max = 80) String sku, @Size(max = 120) String name, UUID categoryId, ProductStatus status) {}
