package com.thiagsilvadev.erpapi.catalog.internal.dto.category;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;

public record CategorySearchCriteria(String code, String name, CategoryStatus status) {}
