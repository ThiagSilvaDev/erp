package com.thiagsilvadev.erpapi.catalog.internal.dto.category;

import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;

public record CategorySearchCriteria(
        @Size(max = 40) String code, @Size(max = 120) String name, CategoryStatus status) {}
