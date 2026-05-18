package com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement;

import java.util.UUID;

public record StockMovementResponse(UUID id, UUID productId, UUID warehouseId, int quantity, String reason) {}
