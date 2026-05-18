package com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation;

import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.CreateReservationRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.service.InventoryService;

@Component
public class CreateReservationUseCase {

    private final InventoryService inventoryService;
    private final InventoryMapper mapper;

    public CreateReservationUseCase(InventoryService inventoryService, InventoryMapper mapper) {
        this.inventoryService = inventoryService;
        this.mapper = mapper;
    }
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).WRITE)")

    public ReservationResponse execute(CreateReservationRequest request) {
        InventoryReservationSnapshot reservation = inventoryService.reserve(
                request.productId(), request.warehouseId(), request.quantity(), request.salesOrderId());
        return mapper.toResponse(reservation);
    }
}
