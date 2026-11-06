package com.thiagsilvadev.erpapi.logistics.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNPROCESSABLE_ENTITY;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CurrentDriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/logistics/drivers/me", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "driver-mobile")
interface DriverMobileApi {

    @GetMapping
    @Operation(operationId = "getCurrentDriver")
    @ApiResponse(responseCode = "200", description = "Current driver retrieved")
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    CurrentDriverResponse getCurrentDriver(@AuthenticationPrincipal Jwt jwt);

    @GetMapping("/shipments")
    @Operation(operationId = "listCurrentDriverShipments")
    @ApiResponse(responseCode = "200", description = "Current driver shipments retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    Page<ShipmentResponse> listShipments(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable);

    @GetMapping("/shipments/{id}")
    @Operation(operationId = "getCurrentDriverShipment")
    @ApiResponse(responseCode = "200", description = "Current driver shipment retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    ShipmentResponse getShipment(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id);

    @PostMapping(value = "/shipments/{id}/load", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "loadCurrentDriverShipment")
    @ApiResponse(responseCode = "200", description = "Current driver shipment loaded")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    ShipmentResponse load(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/dispatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "dispatchCurrentDriverShipment")
    @ApiResponse(responseCode = "200", description = "Current driver shipment dispatched")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ShipmentResponse dispatch(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/tracking-events", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "addCurrentDriverShipmentTrackingEvent")
    @ApiResponse(responseCode = "200", description = "Current driver shipment tracking event added")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    ShipmentResponse addTrackingEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/deliver", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "deliverCurrentDriverShipment")
    @ApiResponse(responseCode = "200", description = "Current driver shipment delivered")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ShipmentResponse deliver(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateTrackingEventRequest request);
}
