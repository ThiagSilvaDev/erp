package com.thiagsilvadev.erpapi.inventory.internal.usecase;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.controller.InventoryService;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.CreateReservationRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.ReservationResponse;

@Component
public class CreateReservationUseCase {

    private final InventoryService inventoryService;

    public CreateReservationUseCase(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).WRITE)")

    public ReservationResponse execute(CreateReservationRequest request) {
        InventoryReservationSnapshot reservation = inventoryService.reserve(
                request.productId(), request.warehouseId(), request.quantity(), request.salesOrderId());
        return new ReservationResponse(
                reservation.id(),
                reservation.productId(),
                reservation.warehouseId(),
                reservation.quantity(),
                reservation.salesOrderId());
    }
}
