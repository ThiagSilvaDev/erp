package com.thiagsilvadev.erpapi.inventory.internal.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReferenceProvider;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationProductDto;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationSalesOrderSummaryResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationWarehouseSummaryResponse;
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

    @Mapping(
            target = "product",
            expression = "java(mapProduct(reservation.getCompanyId(), reservation.getProductId()))")
    @Mapping(target = "warehouse", expression = "java(mapWarehouse(reservation.getWarehouseId()))")
    @Mapping(
            target = "salesOrder",
            expression = "java(mapSalesOrder(reservation.getCompanyId(), reservation.getSalesOrderId()))")
    public abstract ReservationResponse toResponse(InventoryReservation reservation);

    @Mapping(target = "product", expression = "java(mapProduct(reservation.companyId(), reservation.productId()))")
    @Mapping(target = "warehouse", expression = "java(mapWarehouse(reservation.warehouseId()))")
    @Mapping(
            target = "salesOrder",
            expression = "java(mapSalesOrder(reservation.companyId(), reservation.salesOrderId()))")
    public abstract ReservationResponse toResponse(InventoryReservationSnapshot reservation);

    public abstract InventoryReservationSnapshot toSnapshot(InventoryReservation reservation);

    protected ReservationProductDto mapProduct(UUID companyId, UUID productId) {
        var product = catalogFacade.getProduct(companyId, productId);
        return new ReservationProductDto(product.id(), product.sku(), product.name(), product.basePrice());
    }

    protected ReservationWarehouseSummaryResponse mapWarehouse(UUID warehouseId) {
        if (warehouseId == null) {
            return null;
        }
        return warehouseRepository
                .findById(warehouseId)
                .map(warehouse -> new ReservationWarehouseSummaryResponse(warehouse.getId(), warehouse.getCode()))
                .orElse(new ReservationWarehouseSummaryResponse(warehouseId, null));
    }

    protected ReservationSalesOrderSummaryResponse mapSalesOrder(UUID companyId, UUID salesOrderId) {
        if (salesOrderId == null) {
            return null;
        }
        var salesOrder = salesOrderReferenceProvider.getSalesOrderReference(companyId, salesOrderId);
        return new ReservationSalesOrderSummaryResponse(salesOrder.id(), salesOrder.orderNumber());
    }
}
