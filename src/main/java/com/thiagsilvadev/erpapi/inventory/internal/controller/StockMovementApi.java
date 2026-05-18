package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "inventory")
public interface StockMovementApi {

    @PostMapping("/stock-movements")
    @Operation(operationId = "createStockMovement")
    @ApiResponse(responseCode = "201")
    ResponseEntity<StockMovementResponse> createMovement(@Valid @RequestBody CreateStockMovementRequest request);

    @GetMapping("/stock-movements/{id}")
    @Operation(operationId = "getStockMovementById")
    @ApiResponse(responseCode = "200")
    StockMovementResponse getMovement(@PathVariable UUID id);

    @GetMapping("/stock-movements")
    @Operation(operationId = "listStockMovements")
    @ApiResponse(responseCode = "200")
    Page<StockMovementResponse> listMovements(@ParameterObject Pageable pageable);
}
