package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

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
@Tag(name = "inventory", description = "Inventory and stock management endpoints")
public interface InventoryApi {

    @PostMapping("/warehouses")
    @Operation(operationId = "createWarehouse", summary = "Create a warehouse")
    @ApiResponse(responseCode = "201", description = "Warehouse created successfully")
    ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request);

    @GetMapping("/warehouses/{id}")
    @Operation(operationId = "getWarehouseById", summary = "Get a warehouse by ID")
    @ApiResponse(responseCode = "200", description = "Warehouse retrieved successfully")
    WarehouseResponse getWarehouse(@PathVariable UUID id);

    @GetMapping("/warehouses")
    @Operation(operationId = "listWarehouses", summary = "List warehouses")
    @ApiResponse(responseCode = "200", description = "Warehouses listed successfully")
    Page<WarehouseResponse> listWarehouses(Pageable pageable);

    @PostMapping("/stock-movements")
    @Operation(operationId = "createStockMovement", summary = "Create a stock movement")
    @ApiResponse(responseCode = "201", description = "Stock movement created successfully")
    ResponseEntity<StockMovementResponse> createMovement(@Valid @RequestBody CreateStockMovementRequest request);

    @GetMapping("/stock-movements/{id}")
    @Operation(operationId = "getStockMovementById", summary = "Get a stock movement by ID")
    @ApiResponse(responseCode = "200", description = "Stock movement retrieved successfully")
    StockMovementResponse getMovement(@PathVariable UUID id);

    @GetMapping("/stock-movements")
    @Operation(operationId = "listStockMovements", summary = "List stock movements")
    @ApiResponse(responseCode = "200", description = "Stock movements listed successfully")
    Page<StockMovementResponse> listMovements(Pageable pageable);

    @GetMapping("/items")
    @Operation(operationId = "listInventoryItems", summary = "List inventory items")
    @ApiResponse(responseCode = "200", description = "Inventory items listed successfully")
    Page<InventoryItemResponse> listItems(Pageable pageable);

    @GetMapping("/items/{id}")
    @Operation(operationId = "getInventoryItemById", summary = "Get an inventory item by ID")
    @ApiResponse(responseCode = "200", description = "Inventory item retrieved successfully")
    InventoryItemResponse getItem(@PathVariable UUID id);

    @PostMapping("/reservations")
    @Operation(operationId = "createInventoryReservation", summary = "Create an inventory reservation")
    @ApiResponse(responseCode = "201", description = "Inventory reservation created successfully")
    ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request);

    @GetMapping("/reservations/{id}")
    @Operation(operationId = "getInventoryReservationById", summary = "Get an inventory reservation by ID")
    @ApiResponse(responseCode = "200", description = "Inventory reservation retrieved successfully")
    ReservationResponse getReservation(@PathVariable UUID id);

    @GetMapping("/reservations")
    @Operation(operationId = "listInventoryReservations", summary = "List inventory reservations")
    @ApiResponse(responseCode = "200", description = "Inventory reservations listed successfully")
    Page<ReservationResponse> listReservations(Pageable pageable);
}
