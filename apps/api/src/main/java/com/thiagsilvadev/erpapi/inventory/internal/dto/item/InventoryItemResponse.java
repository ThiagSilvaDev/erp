package com.thiagsilvadev.erpapi.inventory.internal.dto.item;

import java.util.UUID;

import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseResponse;

public record InventoryItemResponse(
        UUID id,
        InventoryItemProductResponse product,
        WarehouseResponse warehouse,
        int quantityOnHand,
        int reservedQuantity,
        int availableQuantity) {}
