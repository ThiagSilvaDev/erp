package com.thiagsilvadev.erpapi.catalog.api;

import java.util.UUID;

public record ProductCreatedEvent(UUID productId, String sku) {}
