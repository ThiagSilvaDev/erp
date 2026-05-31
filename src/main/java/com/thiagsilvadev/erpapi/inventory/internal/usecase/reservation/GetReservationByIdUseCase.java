package com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryReservation;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryReservationRepository;

@Component
public class GetReservationByIdUseCase {

    private final InventoryReservationRepository reservationRepository;
    private final ReservationMapper mapper;

    public GetReservationByIdUseCase(InventoryReservationRepository reservationRepository, ReservationMapper mapper) {
        this.reservationRepository = reservationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).READ)")
    public ReservationResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(reservationRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryReservation.class, id)));
    }
}
