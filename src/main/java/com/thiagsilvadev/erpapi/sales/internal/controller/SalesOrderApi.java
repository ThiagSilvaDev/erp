package com.thiagsilvadev.erpapi.sales.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.sales.internal.dto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/sales/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "sales")
public interface SalesOrderApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createSalesOrder")
    @ApiResponse(responseCode = "201")
    ResponseEntity<SalesOrderResponse> createOrder(@Valid @RequestBody CreateSalesOrderRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getSalesOrderById")
    @ApiResponse(responseCode = "200")
    SalesOrderResponse getOrder(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listSalesOrders")
    @ApiResponse(responseCode = "200")
    Page<SalesOrderResponse> listOrders(
            @ParameterObject SalesOrderSearchCriteria criteria, @ParameterObject Pageable pageable);

    @PostMapping("/{id}/confirm")
    @Operation(operationId = "confirmSalesOrder")
    @ApiResponse(responseCode = "200")
    SalesOrderResponse confirmOrder(@PathVariable UUID id);

    @PostMapping("/{id}/cancel")
    @Operation(operationId = "cancelSalesOrder")
    @ApiResponse(responseCode = "200")
    SalesOrderResponse cancelOrder(@PathVariable UUID id);
}
