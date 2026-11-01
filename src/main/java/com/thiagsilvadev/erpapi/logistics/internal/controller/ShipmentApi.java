package com.thiagsilvadev.erpapi.logistics.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNPROCESSABLE_ENTITY;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/logistics", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "logistics")
public interface ShipmentApi {

    @GetMapping("/shipments")
    @Operation(operationId = "listShipments")
    @ApiResponse(responseCode = "200", description = "Shipments retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    Page<ShipmentResponse> list(@ParameterObject ShipmentSearchCriteria criteria, @ParameterObject Pageable pageable);

    @GetMapping("/shipments/{id}")
    @Operation(operationId = "getShipmentById")
    @ApiResponse(responseCode = "200", description = "Shipment retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    ShipmentResponse getById(@PathVariable UUID id);

    @PostMapping(value = "/shipments/{id}/dispatch-assignment", consumes = "application/json")
    @Operation(operationId = "assignShipmentDispatch")
    @ApiResponse(responseCode = "200", description = "Shipment dispatch assigned")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    ShipmentResponse assignDispatch(@PathVariable UUID id, @Valid @RequestBody AssignDispatchRequest request);

    @PostMapping(value = "/shipments/{id}/load", consumes = "application/json")
    @Operation(operationId = "loadShipment")
    @ApiResponse(responseCode = "200", description = "Shipment loaded")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    ShipmentResponse load(@PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/dispatch", consumes = "application/json")
    @Operation(operationId = "dispatchShipment")
    @ApiResponse(responseCode = "200", description = "Shipment dispatched")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ShipmentResponse dispatch(@PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/tracking-events", consumes = "application/json")
    @Operation(operationId = "addShipmentTrackingEvent")
    @ApiResponse(responseCode = "200", description = "Shipment tracking event added")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    ShipmentResponse addTrackingEvent(
            @PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/deliver", consumes = "application/json")
    @Operation(operationId = "deliverShipment")
    @ApiResponse(responseCode = "200", description = "Shipment delivered")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ShipmentResponse deliver(@PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/cancel", consumes = "application/json")
    @Operation(operationId = "cancelShipment")
    @ApiResponse(responseCode = "200", description = "Shipment cancelled")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ShipmentResponse cancel(@PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request);
}
