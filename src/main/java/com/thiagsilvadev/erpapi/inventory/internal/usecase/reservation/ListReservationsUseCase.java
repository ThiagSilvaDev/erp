package com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryReservationRepository;
import com.thiagsilvadev.erpapi.inventory.internal.service.ReservationResponseAssembler;

@Component
public class ListReservationsUseCase {

    private final InventoryReservationRepository reservationRepository;
    private final ReservationResponseAssembler assembler;

    public ListReservationsUseCase(
            InventoryReservationRepository reservationRepository, ReservationResponseAssembler assembler) {
        this.reservationRepository = reservationRepository;
        this.assembler = assembler;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<ReservationResponse> execute(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return reservationRepository.findAllByCompanyId(companyId, pageable).map(assembler::toResponse);
    }
}
