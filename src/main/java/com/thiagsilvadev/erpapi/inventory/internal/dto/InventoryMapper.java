package com.thiagsilvadev.erpapi.inventory.internal.dto;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.inventory.api.InventoryItemSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.WarehouseSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.*;
import com.thiagsilvadev.erpapi.inventory.internal.model.*;

@Component
public class InventoryMapper {

    public WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(warehouse.getId(), warehouse.getCode(), warehouse.getName());
    }

    public InventoryItemResponse toResponse(InventoryItem item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getProductId(),
                item.getWarehouse().getId(),
                item.getQuantityOnHand(),
                item.getReservedQuantity(),
                item.availableQuantity());
    }

    public StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProductId(),
                movement.getWarehouseId(),
                movement.getQuantity(),
                movement.getReason());
    }

    public ReservationResponse toResponse(InventoryReservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getProductId(),
                reservation.getWarehouseId(),
                reservation.getQuantity(),
                reservation.getSalesOrderId());
    }

    public InventoryItemSnapshot toSnapshot(InventoryItem item) {
        return new InventoryItemSnapshot(
                item.getId(),
                item.getProductId(),
                item.getWarehouse().getId(),
                item.getQuantityOnHand(),
                item.getReservedQuantity());
    }

    public WarehouseSnapshot toSnapshot(Warehouse warehouse) {
        return new WarehouseSnapshot(warehouse.getId(), warehouse.getCode(), warehouse.getName());
    }

    public InventoryReservationSnapshot toSnapshot(InventoryReservation reservation) {
        return new InventoryReservationSnapshot(
                reservation.getId(),
                reservation.getProductId(),
                reservation.getWarehouseId(),
                reservation.getQuantity(),
                reservation.getSalesOrderId());
    }
}
