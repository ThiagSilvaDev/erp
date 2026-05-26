package com.thiagsilvadev.erpapi.inventory.internal.dto.reservation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReferenceProvider;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryReservation;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Mapper(componentModel = "spring")
public abstract class ReservationMapper {

    protected CatalogFacade catalogFacade;
    protected WarehouseRepository warehouseRepository;
    protected SalesOrderReferenceProvider salesOrderReferenceProvider;

    @Autowired
    public void setCatalogFacade(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }

    @Autowired
    public void setWarehouseRepository(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Autowired
    public void setSalesOrderReferenceProvider(SalesOrderReferenceProvider salesOrderReferenceProvider) {
        this.salesOrderReferenceProvider = salesOrderReferenceProvider;
    }

    @Mapping(target = "product", expression = "java(mapProduct(reservation.getProductId()))")
    @Mapping(target = "warehouse", expression = "java(mapWarehouse(reservation.getWarehouseId()))")
    @Mapping(target = "salesOrder", expression = "java(mapSalesOrder(reservation.getSalesOrderId()))")
    public abstract ReservationResponse toResponse(InventoryReservation reservation);

    @Mapping(target = "product", expression = "java(mapProduct(reservation.productId()))")
    @Mapping(target = "warehouse", expression = "java(mapWarehouse(reservation.warehouseId()))")
    @Mapping(target = "salesOrder", expression = "java(mapSalesOrder(reservation.salesOrderId()))")
    public abstract ReservationResponse toResponse(InventoryReservationSnapshot reservation);

    public abstract InventoryReservationSnapshot toSnapshot(InventoryReservation reservation);

    protected ReservationProductDto mapProduct(java.util.UUID productId) {
        return catalogFacade
                .findProductById(productId)
                .map(p -> new ReservationProductDto(p.id(), p.sku(), p.name(), p.basePrice()))
                .orElse(null);
    }

    protected ReservationWarehouseSummaryResponse mapWarehouse(java.util.UUID warehouseId) {
        if (warehouseId == null) {
            return null;
        }
        return warehouseRepository
                .findById(warehouseId)
                .map(warehouse -> new ReservationWarehouseSummaryResponse(warehouse.getId(), warehouse.getCode()))
                .orElse(new ReservationWarehouseSummaryResponse(warehouseId, null));
    }

    protected ReservationSalesOrderSummaryResponse mapSalesOrder(java.util.UUID salesOrderId) {
        if (salesOrderId == null) {
            return null;
        }
        return salesOrderReferenceProvider
                .findSalesOrderReferenceById(salesOrderId)
                .map(salesOrder -> new ReservationSalesOrderSummaryResponse(salesOrder.id(), salesOrder.orderNumber()))
                .orElse(new ReservationSalesOrderSummaryResponse(salesOrderId, null));
    }
}
