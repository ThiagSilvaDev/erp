package com.thiagsilvadev.erpapi.inventory.api;

import java.util.UUID;

public record InventoryReservationSnapshot(
        UUID id, UUID companyId, UUID productId, UUID warehouseId, int quantity, UUID salesOrderId) {}
