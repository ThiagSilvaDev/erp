package com.thiagsilvadev.erpapi.inventory.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReferenceProvider;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationProductDto;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationSalesOrderSummaryResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationWarehouseSummaryResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryReservation;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

/**
 * Builds reservation responses. It resolves products, warehouses and sales order references, so it is
 * a response assembler rather than a mapper; mapping a collection costs one lookup per reservation.
 */
@Component
public class ReservationResponseAssembler {

    private final CatalogFacade catalogFacade;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderReferenceProvider salesOrderReferenceProvider;

    public ReservationResponseAssembler(
            CatalogFacade catalogFacade,
            WarehouseRepository warehouseRepository,
            SalesOrderReferenceProvider salesOrderReferenceProvider) {
        this.catalogFacade = catalogFacade;
        this.warehouseRepository = warehouseRepository;
        this.salesOrderReferenceProvider = salesOrderReferenceProvider;
    }

    public ReservationResponse toResponse(InventoryReservation reservation) {
        return toResponse(
                reservation.getId(),
                reservation.getCompanyId(),
                reservation.getProductId(),
                reservation.getWarehouseId(),
                reservation.getQuantity(),
                reservation.getSalesOrderId());
    }

    public ReservationResponse toResponse(InventoryReservationSnapshot reservation) {
        return toResponse(
                reservation.id(),
                reservation.companyId(),
                reservation.productId(),
                reservation.warehouseId(),
                reservation.quantity(),
                reservation.salesOrderId());
    }

    public InventoryReservationSnapshot toSnapshot(InventoryReservation reservation) {
        return new InventoryReservationSnapshot(
                reservation.getId(),
                reservation.getCompanyId(),
                reservation.getProductId(),
                reservation.getWarehouseId(),
                reservation.getQuantity(),
                reservation.getSalesOrderId());
    }

    private ReservationResponse toResponse(
            UUID id, UUID companyId, UUID productId, UUID warehouseId, int quantity, UUID salesOrderId) {
        return new ReservationResponse(
                id,
                toProduct(companyId, productId),
                toWarehouse(warehouseId),
                quantity,
                toSalesOrder(companyId, salesOrderId));
    }

    private ReservationProductDto toProduct(UUID companyId, UUID productId) {
        ProductSnapshot product = catalogFacade.getProduct(companyId, productId);
        return new ReservationProductDto(product.id(), product.sku(), product.name(), product.basePrice());
    }

    private ReservationWarehouseSummaryResponse toWarehouse(UUID warehouseId) {
        if (warehouseId == null) {
            return null;
        }

        return warehouseRepository
                .findById(warehouseId)
                .map(warehouse -> new ReservationWarehouseSummaryResponse(warehouse.getId(), warehouse.getCode()))
                .orElse(new ReservationWarehouseSummaryResponse(warehouseId, null));
    }

    private ReservationSalesOrderSummaryResponse toSalesOrder(UUID companyId, UUID salesOrderId) {
        if (salesOrderId == null) {
            return null;
        }

        var salesOrder = salesOrderReferenceProvider.getSalesOrderReference(companyId, salesOrderId);
        return new ReservationSalesOrderSummaryResponse(salesOrder.id(), salesOrder.orderNumber());
    }
}
