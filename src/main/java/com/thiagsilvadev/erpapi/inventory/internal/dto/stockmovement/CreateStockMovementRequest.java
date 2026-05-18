package com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateStockMovementRequest(
        @NotNull UUID productId, @NotNull UUID warehouseId, @Positive int quantity, @NotBlank String reason) {}
