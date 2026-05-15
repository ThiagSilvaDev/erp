package com.thiagsilvadev.erpapi.inventory.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class InventoryDtos {

    private InventoryDtos() {}

    public record CreateWarehouseRequest(@NotBlank String code, @NotBlank String name) {}

    public record WarehouseResponse(UUID id, String code, String name) {}

    public record CreateStockMovementRequest(
            @NotNull UUID productId, @NotNull UUID warehouseId, @Positive int quantity, @NotBlank String reason) {}

    public record StockMovementResponse(UUID id, UUID productId, UUID warehouseId, int quantity, String reason) {}

    public record InventoryItemResponse(
            UUID id,
            UUID productId,
            UUID warehouseId,
            int quantityOnHand,
            int reservedQuantity,
            int availableQuantity) {}

    public record CreateReservationRequest(
            @NotNull UUID productId, @NotNull UUID warehouseId, @Positive int quantity, @NotNull UUID salesOrderId) {}

    public record ReservationResponse(UUID id, UUID productId, UUID warehouseId, int quantity, UUID salesOrderId) {}
}
