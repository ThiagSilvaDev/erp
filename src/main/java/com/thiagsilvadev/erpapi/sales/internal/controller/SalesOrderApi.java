package com.thiagsilvadev.erpapi.sales.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNPROCESSABLE_ENTITY;

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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/sales/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "sales")
public interface SalesOrderApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createSalesOrder")
    @ApiResponse(responseCode = "201", description = "Sales order created")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody CreateSalesOrderRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getSalesOrderById")
    @ApiResponse(responseCode = "200", description = "Sales order retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    SalesOrderResponse getById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listSalesOrders")
    @ApiResponse(responseCode = "200", description = "Sales orders retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    Page<SalesOrderResponse> list(
            @Valid @ParameterObject SalesOrderSearchCriteria criteria, @ParameterObject Pageable pageable);

    @PostMapping("/{id}/confirm")
    @Operation(operationId = "confirmSalesOrder")
    @ApiResponse(responseCode = "200", description = "Sales order confirmed")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    SalesOrderResponse confirm(@PathVariable UUID id);

    @PostMapping("/{id}/cancel")
    @Operation(operationId = "cancelSalesOrder")
    @ApiResponse(responseCode = "200", description = "Sales order cancelled")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    SalesOrderResponse cancel(@PathVariable UUID id);
}
