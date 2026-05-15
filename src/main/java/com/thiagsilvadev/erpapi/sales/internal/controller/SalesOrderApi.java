package com.thiagsilvadev.erpapi.sales.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.sales.internal.dto.SalesDtos.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/sales/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "sales", description = "Sales order management endpoints")
public interface SalesOrderApi {

    @PostMapping
    @Operation(operationId = "createSalesOrder", summary = "Create a sales order")
    @ApiResponse(responseCode = "201", description = "Sales order created successfully")
    ResponseEntity<SalesOrderResponse> createOrder(@Valid @RequestBody CreateSalesOrderRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getSalesOrderById", summary = "Get a sales order by ID")
    @ApiResponse(responseCode = "200", description = "Sales order retrieved successfully")
    SalesOrderResponse getOrder(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listSalesOrders", summary = "List sales orders")
    @ApiResponse(responseCode = "200", description = "Sales orders listed successfully")
    Page<SalesOrderResponse> listOrders(SalesOrderSearchCriteria criteria, Pageable pageable);

    @PostMapping("/{id}/confirm")
    @Operation(operationId = "confirmSalesOrder", summary = "Confirm a sales order")
    @ApiResponse(responseCode = "200", description = "Sales order confirmed successfully")
    SalesOrderResponse confirmOrder(@PathVariable UUID id);

    @PostMapping("/{id}/cancel")
    @Operation(operationId = "cancelSalesOrder", summary = "Cancel a sales order")
    @ApiResponse(responseCode = "200", description = "Sales order cancelled successfully")
    SalesOrderResponse cancelOrder(@PathVariable UUID id);
}
