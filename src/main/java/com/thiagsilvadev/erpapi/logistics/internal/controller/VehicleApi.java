package com.thiagsilvadev.erpapi.logistics.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;

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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/logistics", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "logistics")
public interface VehicleApi {

    @PostMapping(value = "/vehicles", consumes = "application/json")
    @Operation(operationId = "createVehicle")
    @ApiResponse(responseCode = "201", description = "Vehicle created")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest request);

    @GetMapping("/vehicles/{id}")
    @Operation(operationId = "getVehicleById")
    @ApiResponse(responseCode = "200", description = "Vehicle retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    VehicleResponse getById(@PathVariable UUID id);

    @GetMapping("/vehicles")
    @Operation(operationId = "listVehicles")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    Page<VehicleResponse> list(@ParameterObject Pageable pageable);
}
