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

@RequestMapping(value = "/api/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "customers")
public interface CustomerApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createCustomer")
    @ApiResponse(responseCode = "201")
    ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getCustomerById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<CustomerResponse> getCustomerById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listCustomers")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<CustomerResponse>> listCustomers(
            @ParameterObject CustomerSearchCriteria criteria, @ParameterObject Pageable pageable);
}
