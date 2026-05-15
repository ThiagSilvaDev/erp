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

@RequestMapping(value = "/api/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "customers", description = "Customer management endpoints")
public interface CustomerApi {

    @PostMapping
    @Operation(operationId = "createCustomer", summary = "Create a customer")
    @ApiResponse(responseCode = "201", description = "Customer created successfully")
    ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getCustomerById", summary = "Get a customer by ID")
    @ApiResponse(responseCode = "200", description = "Customer retrieved successfully")
    ResponseEntity<CustomerResponse> getCustomerById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listCustomers", summary = "List customers")
    @ApiResponse(responseCode = "200", description = "Customers listed successfully")
    ResponseEntity<Page<CustomerResponse>> listCustomers(CustomerSearchCriteria criteria, Pageable pageable);
}
