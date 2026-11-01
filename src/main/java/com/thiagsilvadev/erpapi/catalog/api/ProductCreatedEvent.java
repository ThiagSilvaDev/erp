package com.thiagsilvadev.erpapi.catalog.api;

import java.util.UUID;

/**
 * Published through the catalog module event publisher whenever a product is created.
 *
 * <p>This record is part of the module's public contract and is deliberately published without
 * in-process subscribers: it reserves the integration point for future consumers such as stock
 * provisioning or outbound webhooks. Do not remove it as unused code; add a subscriber instead.
 */
public record ProductCreatedEvent(UUID productId, String sku) {}
