package com.thiagsilvadev.erpapi.inventory.internal.mapper;

import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;

public final class StockMovementMapper {

    private StockMovementMapper() {}

    public static StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getMovementNumber(),
                movement.getProductId(),
                movement.getWarehouseId(),
                movement.getQuantity(),
                movement.getType(),
                movement.getReason());
    }
}
