package com.thiagsilvadev.erpapi.catalog.internal.service;

import java.util.UUID;

public record SkuGenerationContext(UUID companyId, String categoryCode) {}
