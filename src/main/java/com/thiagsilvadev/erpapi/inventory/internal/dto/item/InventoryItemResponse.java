package com.thiagsilvadev.erpapi.inventory.internal.dto.item;

import java.util.UUID;

public record InventoryItemResponse(
        UUID id,
        UUID productId,
        UUID warehouseId,
        int quantityOnHand,
        int reservedQuantity,
        int availableQuantity) {}
