package com.thiagsilvadev.erpapi.inventory.internal.dto.item;

import java.util.UUID;

public record InventoryItemCategoryResponse(UUID id, String code, String name) {}
