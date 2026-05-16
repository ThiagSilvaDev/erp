package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "inventory")
public interface InventoryApi {

    @PostMapping("/warehouses")
    @Operation(operationId = "createWarehouse")
    @ApiResponse(responseCode = "201")
    ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request);

    @GetMapping("/warehouses/{id}")
    @Operation(operationId = "getWarehouseById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<WarehouseResponse> getWarehouse(@PathVariable UUID id);

    @GetMapping("/warehouses")
    @Operation(operationId = "listWarehouses")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<WarehouseResponse>> listWarehouses(@ParameterObject Pageable pageable);

    @PostMapping("/stock-movements")
    @Operation(operationId = "createStockMovement")
    @ApiResponse(responseCode = "201")
    ResponseEntity<StockMovementResponse> createMovement(@Valid @RequestBody CreateStockMovementRequest request);

    @GetMapping("/stock-movements/{id}")
    @Operation(operationId = "getStockMovementById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<StockMovementResponse> getMovement(@PathVariable UUID id);

    @GetMapping("/stock-movements")
    @Operation(operationId = "listStockMovements")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<StockMovementResponse>> listMovements(@ParameterObject Pageable pageable);

    @GetMapping("/items")
    @Operation(operationId = "listInventoryItems")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<InventoryItemResponse>> listItems(@ParameterObject Pageable pageable);

    @GetMapping("/items/{id}")
    @Operation(operationId = "getInventoryItemById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<InventoryItemResponse> getItem(@PathVariable UUID id);

    @PostMapping(value = "/reservations", consumes = "application/json")
    @Operation(operationId = "createInventoryReservation")
    @ApiResponse(responseCode = "201")
    ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request);

    @GetMapping("/reservations/{id}")
    @Operation(operationId = "getInventoryReservationById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<ReservationResponse> getReservation(@PathVariable UUID id);

    @GetMapping("/reservations")
    @Operation(operationId = "listInventoryReservations")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<ReservationResponse>> listReservations(@ParameterObject Pageable pageable);
}
