package com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.ReserveStockCommand;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.CreateReservationRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;

@Component
public class CreateReservationUseCase {

    private final InventoryFacade inventoryFacade;
    private final ReservationMapper mapper;

    public CreateReservationUseCase(InventoryFacade inventoryFacade, ReservationMapper mapper) {
        this.inventoryFacade = inventoryFacade;
        this.mapper = mapper;
    }

    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).WRITE)")
    public ReservationResponse execute(CreateReservationRequest request) {
        var companyId = TenantContext.requireTenantId();
        InventoryReservationSnapshot reservation = inventoryFacade.reserveStock(new ReserveStockCommand(
                companyId, request.productId(), request.warehouseId(), request.quantity(), request.salesOrderId()));
        return mapper.toResponse(reservation);
    }
}
