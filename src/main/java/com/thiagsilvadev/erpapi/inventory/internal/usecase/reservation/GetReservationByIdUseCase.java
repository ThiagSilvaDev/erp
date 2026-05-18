package com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation;

import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;

import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryReservation;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryReservationRepository;

@Component
public class GetReservationByIdUseCase {

    private final InventoryReservationRepository reservationRepository;
    private final InventoryMapper mapper;

    public GetReservationByIdUseCase(InventoryReservationRepository reservationRepository, InventoryMapper mapper) {
        this.reservationRepository = reservationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).READ)")
    public ReservationResponse execute(UUID id) {
        return mapper.toResponse(reservationRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryReservation.class, id)));
    }
}
