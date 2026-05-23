package com.thiagsilvadev.erpapi.catalog.api;

import java.util.UUID;

public record ProductCreatedEvent(UUID productId, String sku) {

    public static ProductCreatedEvent created(UUID productId, String sku) {
        return new ProductCreatedEvent(productId, sku);
    }
}
