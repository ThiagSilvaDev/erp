package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "inventory")
public interface WarehouseApi {

    @PostMapping("/warehouses")
    @Operation(operationId = "createWarehouse")
    @ApiResponse(responseCode = "201")
    ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request);

    @GetMapping("/warehouses/{id}")
    @Operation(operationId = "getWarehouseById")
    @ApiResponse(responseCode = "200")
    WarehouseResponse getWarehouse(@PathVariable UUID id);

    @GetMapping("/warehouses")
    @Operation(operationId = "listWarehouses")
    @ApiResponse(responseCode = "200")
    Page<WarehouseResponse> listWarehouses(@ParameterObject Pageable pageable);
}
