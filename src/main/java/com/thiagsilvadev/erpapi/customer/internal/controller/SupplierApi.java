package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "suppliers")
public interface SupplierApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createSupplier")
    @ApiResponse(responseCode = "201")
    ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getSupplierById")
    @ApiResponse(responseCode = "200")
    SupplierResponse getSupplierById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listSuppliers")
    @ApiResponse(responseCode = "200")
    Page<SupplierResponse> listSuppliers(
            @ParameterObject SupplierSearchCriteria criteria, @ParameterObject Pageable pageable);
}
