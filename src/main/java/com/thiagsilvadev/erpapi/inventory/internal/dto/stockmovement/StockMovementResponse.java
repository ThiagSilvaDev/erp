package com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement;

import java.util.UUID;

import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementReason;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementType;

public record StockMovementResponse(
        UUID id,
        Long movementNumber,
        UUID productId,
        UUID warehouseId,
        int quantity,
        StockMovementType type,
        StockMovementReason reason) {}
