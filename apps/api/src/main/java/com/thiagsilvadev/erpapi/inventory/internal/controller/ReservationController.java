package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.*;
import com.thiagsilvadev.erpapi.inventory.internal.usecase.reservation.*;

@RestController
class ReservationController implements ReservationApi {

    private final CreateReservationUseCase createReservationUseCase;
    private final ReservationQueryUseCase reservationQueryUseCase;

    ReservationController(
            CreateReservationUseCase createReservationUseCase, ReservationQueryUseCase reservationQueryUseCase) {
        this.createReservationUseCase = createReservationUseCase;
        this.reservationQueryUseCase = reservationQueryUseCase;
    }

    @Override
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = createReservationUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public ReservationResponse getById(@PathVariable UUID id) {
        return reservationQueryUseCase.getById(id);
    }

    @Override
    public Page<ReservationResponse> list(Pageable pageable) {
        return reservationQueryUseCase.list(pageable);
    }
}
