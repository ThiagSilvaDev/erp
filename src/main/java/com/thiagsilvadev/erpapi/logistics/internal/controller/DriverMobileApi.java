package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CurrentDriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/logistics/drivers/me", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "driver mobile")
public interface DriverMobileApi {

    @GetMapping
    @Operation(operationId = "getCurrentDriver")
    @ApiResponse(responseCode = "200")
    CurrentDriverResponse getCurrentDriver(@AuthenticationPrincipal Jwt jwt);

    @GetMapping("/shipments")
    @Operation(operationId = "listCurrentDriverShipments")
    @ApiResponse(responseCode = "200")
    Page<ShipmentResponse> listShipments(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable);

    @GetMapping("/shipments/{id}")
    @Operation(operationId = "getCurrentDriverShipment")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "401")
    @ApiResponse(responseCode = "403")
    @ApiResponse(responseCode = "404")
    ShipmentResponse getShipment(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id);

    @PostMapping(value = "/shipments/{id}/load", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "loadCurrentDriverShipment")
    @ApiResponse(responseCode = "200")
    ShipmentResponse load(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/dispatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "dispatchCurrentDriverShipment")
    @ApiResponse(responseCode = "200")
    ShipmentResponse dispatch(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/tracking-events", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "addCurrentDriverShipmentTrackingEvent")
    @ApiResponse(responseCode = "200")
    ShipmentResponse addTrackingEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateTrackingEventRequest request);

    @PostMapping(value = "/shipments/{id}/deliver", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "deliverCurrentDriverShipment")
    @ApiResponse(responseCode = "200")
    ShipmentResponse deliver(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateTrackingEventRequest request);
}
