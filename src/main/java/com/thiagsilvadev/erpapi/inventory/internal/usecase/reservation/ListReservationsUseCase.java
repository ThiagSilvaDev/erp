package com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation;

import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;

import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryReservationRepository;

@Component
public class ListReservationsUseCase {

    private final InventoryReservationRepository reservationRepository;
    private final InventoryMapper mapper;

    public ListReservationsUseCase(InventoryReservationRepository reservationRepository, InventoryMapper mapper) {
        this.reservationRepository = reservationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).READ)")
    public Page<ReservationResponse> execute(Pageable pageable) {
        return reservationRepository.findAll(pageable).map(mapper::toResponse);
    }
}
