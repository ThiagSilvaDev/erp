package com.thiagsilvadev.erpapi.catalog.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;

public record ProductSearchCriteria(String sku, String name, UUID categoryId, ProductStatus status) {}
