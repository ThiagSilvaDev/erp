package com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryReservationRepository;

@Component
public class ListReservationsUseCase {

    private final InventoryReservationRepository reservationRepository;
    private final ReservationMapper mapper;

    public ListReservationsUseCase(InventoryReservationRepository reservationRepository, ReservationMapper mapper) {
        this.reservationRepository = reservationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<ReservationResponse> execute(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return reservationRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
