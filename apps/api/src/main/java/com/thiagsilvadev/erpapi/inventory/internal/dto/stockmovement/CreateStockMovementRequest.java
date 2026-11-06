package com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementReason;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementType;

public record CreateStockMovementRequest(
        @NotNull UUID productId,
        @NotNull UUID warehouseId,
        @Positive int quantity,
        @NotNull StockMovementType type,
        @NotNull StockMovementReason reason) {}
