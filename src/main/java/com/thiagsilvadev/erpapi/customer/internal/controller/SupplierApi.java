package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.customer.internal.dto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/suppliers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "suppliers", description = "Supplier management endpoints")
public interface SupplierApi {

    @PostMapping
    @Operation(operationId = "createSupplier", summary = "Create a supplier")
    @ApiResponse(responseCode = "201", description = "Supplier created successfully")
    ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getSupplierById", summary = "Get a supplier by ID")
    @ApiResponse(responseCode = "200", description = "Supplier retrieved successfully")
    ResponseEntity<SupplierResponse> getSupplierById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listSuppliers", summary = "List suppliers")
    @ApiResponse(responseCode = "200", description = "Suppliers listed successfully")
    ResponseEntity<Page<SupplierResponse>> listSuppliers(CustomerSearchCriteria criteria, Pageable pageable);
}
