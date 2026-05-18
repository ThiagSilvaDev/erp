package com.thiagsilvadev.erpapi.inventory.internal.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryItemSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.WarehouseSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemCategoryResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemProductResponse;
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
    @Mapping(target = "product", expression = "java(mapInventoryItemProduct(item.getProductId()))")
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

    protected InventoryItemProductResponse mapInventoryItemProduct(java.util.UUID productId) {
        return catalogFacade
                .findProductById(productId)
                .map(this::toInventoryItemProductResponse)
                .orElse(null);
    }

    protected InventoryItemProductResponse toInventoryItemProductResponse(ProductSnapshot product) {
        return new InventoryItemProductResponse(
                product.id(), product.sku(), product.name(), toInventoryItemCategoryResponse(product.category()));
    }

    protected InventoryItemCategoryResponse toInventoryItemCategoryResponse(CategorySnapshot category) {
        if (category == null) {
            return null;
        }
        return new InventoryItemCategoryResponse(category.id(), category.code(), category.name());
    }

    @Mapping(target = "warehouseId", source = "warehouse.id")
    public abstract InventoryItemSnapshot toSnapshot(InventoryItem item);

    public abstract WarehouseSnapshot toSnapshot(Warehouse warehouse);

    public abstract InventoryReservationSnapshot toSnapshot(InventoryReservation reservation);
}
