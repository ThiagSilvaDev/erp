package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.logistics.internal.dto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/logistics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "logistics")
public interface ShipmentApi {

    @GetMapping("/shipments")
    @Operation(operationId = "listShipments")
    @ApiResponse(responseCode = "200")
    Page<ShipmentResponse> listShipments(ShipmentSearchCriteria criteria, @ParameterObject Pageable pageable);

    @GetMapping("/shipments/{id}")
    @Operation(operationId = "getShipmentById")
    @ApiResponse(responseCode = "200")
    ShipmentResponse getShipment(@PathVariable UUID id);

    @PostMapping(value = "/shipments/{id}/dispatch-assignment", consumes = "application/json")
    @Operation(operationId = "assignShipmentDispatch")
    @ApiResponse(responseCode = "200")
    ShipmentResponse assignDispatch(@PathVariable UUID id, @Valid @RequestBody AssignDispatchRequest request);

    @PostMapping(value = "/shipments/{id}/load", consumes = "application/json")
    @Operation(operationId = "loadShipment")
    @ApiResponse(responseCode = "200")
    ShipmentResponse load(@PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/dispatch", consumes = "application/json")
    @Operation(operationId = "dispatchShipment")
    @ApiResponse(responseCode = "200")
    ShipmentResponse dispatch(@PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/tracking-events", consumes = "application/json")
    @Operation(operationId = "addShipmentTrackingEvent")
    @ApiResponse(responseCode = "200")
    ShipmentResponse addTrackingEvent(
            @PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/deliver", consumes = "application/json")
    @Operation(operationId = "deliverShipment")
    @ApiResponse(responseCode = "200")
    ShipmentResponse deliver(@PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/cancel", consumes = "application/json")
    @Operation(operationId = "cancelShipment")
    @ApiResponse(responseCode = "200")
    ShipmentResponse cancel(@PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);
}
