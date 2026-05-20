package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle.CreateVehicleRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle.VehicleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/logistics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "logistics")
public interface VehicleApi {

    @PostMapping(value = "/vehicles", consumes = "application/json")
    @Operation(operationId = "createVehicle")
    @ApiResponse(responseCode = "201")
    ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request);

    @GetMapping("/vehicles/{id}")
    @Operation(operationId = "getVehicleById")
    @ApiResponse(responseCode = "200")
    VehicleResponse getVehicle(@PathVariable UUID id);

    @GetMapping("/vehicles")
    @Operation(operationId = "listVehicles")
    @ApiResponse(responseCode = "200")
    Page<VehicleResponse> listVehicles(@ParameterObject Pageable pageable);
}
