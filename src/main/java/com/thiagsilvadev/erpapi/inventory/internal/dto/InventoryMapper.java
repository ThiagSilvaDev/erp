package com.thiagsilvadev.erpapi.inventory.internal.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryItemSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.WarehouseSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationProductDto;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.*;

@Mapper(componentModel = "spring")
public abstract class InventoryMapper {

    protected CatalogFacade catalogFacade;

    @Autowired
    public void setCatalogFacade(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }

    public abstract WarehouseResponse toResponse(Warehouse warehouse);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "availableQuantity", expression = "java(item.availableQuantity())")
    public abstract InventoryItemResponse toResponse(InventoryItem item);

    public abstract StockMovementResponse toResponse(StockMovement movement);

    @Mapping(target = "product", expression = "java(mapProduct(reservation.getProductId()))")
    public abstract ReservationResponse toResponse(InventoryReservation reservation);

    @Mapping(target = "product", expression = "java(mapProduct(reservation.productId()))")
    public abstract ReservationResponse toResponse(InventoryReservationSnapshot reservation);

    protected ReservationProductDto mapProduct(java.util.UUID productId) {
        return catalogFacade
                .findProductById(productId)
                .map(p -> new ReservationProductDto(p.id(), p.sku(), p.name(), p.basePrice()))
                .orElse(null);
    }

    @Mapping(target = "warehouseId", source = "warehouse.id")
    public abstract InventoryItemSnapshot toSnapshot(InventoryItem item);

    public abstract WarehouseSnapshot toSnapshot(Warehouse warehouse);

    public abstract InventoryReservationSnapshot toSnapshot(InventoryReservation reservation);
}
