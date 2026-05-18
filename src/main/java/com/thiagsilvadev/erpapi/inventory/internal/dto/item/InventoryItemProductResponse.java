package com.thiagsilvadev.erpapi.inventory.internal.dto.item;

import java.util.UUID;

public record InventoryItemProductResponse(UUID id, String sku, String name, InventoryItemCategoryResponse category) {}
