package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "inventory")
public interface ReservationApi {

    @PostMapping(value = "/reservations", consumes = "application/json")
    @Operation(operationId = "createInventoryReservation")
    @ApiResponse(responseCode = "201")
    ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request);

    @GetMapping("/reservations/{id}")
    @Operation(operationId = "getInventoryReservationById")
    @ApiResponse(responseCode = "200")
    ReservationResponse getReservation(@PathVariable UUID id);

    @GetMapping("/reservations")
    @Operation(operationId = "listInventoryReservations")
    @ApiResponse(responseCode = "200")
    Page<ReservationResponse> listReservations(@ParameterObject Pageable pageable);
}
