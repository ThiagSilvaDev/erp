package com.thiagsilvadev.erpapi.inventory.api;

import java.util.UUID;

public record InventoryItemSnapshot(
        UUID id, UUID productId, UUID warehouseId, int quantityOnHand, int reservedQuantity) {

    public int availableQuantity() {
        return quantityOnHand - reservedQuantity;
    }
}
