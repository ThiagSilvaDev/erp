package com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.WRITE;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.ReserveStockCommand;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.CreateReservationRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;
import com.thiagsilvadev.erpapi.inventory.internal.service.ReservationResponseAssembler;

@Component
public class CreateReservationUseCase {

    private final InventoryFacade inventoryFacade;
    private final ReservationResponseAssembler assembler;

    public CreateReservationUseCase(InventoryFacade inventoryFacade, ReservationResponseAssembler assembler) {
        this.inventoryFacade = inventoryFacade;
        this.assembler = assembler;
    }

    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public ReservationResponse execute(CreateReservationRequest request) {
        var companyId = TenantContext.requireTenantId();
        InventoryReservationSnapshot reservation = inventoryFacade.reserveStock(new ReserveStockCommand(
                companyId, request.productId(), request.warehouseId(), request.quantity(), request.salesOrderId()));
        return assembler.toResponse(reservation);
    }
}
