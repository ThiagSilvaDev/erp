package com.thiagsilvadev.erpapi.catalog.internal.dto.category;

import java.util.UUID;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;

public record CategoryResponse(UUID id, String code, String name, String description, CategoryStatus status) {}
