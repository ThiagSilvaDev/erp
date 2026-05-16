package com.thiagsilvadev.erpapi.inventory.internal.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.inventory.api.InventoryItemSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.WarehouseSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.*;
import com.thiagsilvadev.erpapi.inventory.internal.model.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    WarehouseResponse toResponse(Warehouse warehouse);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "availableQuantity", expression = "java(item.availableQuantity())")
    InventoryItemResponse toResponse(InventoryItem item);

    StockMovementResponse toResponse(StockMovement movement);

    ReservationResponse toResponse(InventoryReservation reservation);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    InventoryItemSnapshot toSnapshot(InventoryItem item);

    WarehouseSnapshot toSnapshot(Warehouse warehouse);

    InventoryReservationSnapshot toSnapshot(InventoryReservation reservation);
}
