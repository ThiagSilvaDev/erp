package com.thiagsilvadev.erpapi.catalog.internal.dto.category;

import java.util.UUID;

public record CategorySummaryResponse(UUID id, String code, String name) {}
