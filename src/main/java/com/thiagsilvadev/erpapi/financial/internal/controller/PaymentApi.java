package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreatePaymentRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.PaymentResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "financial payments")
interface PaymentApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createPayment")
    @ApiResponse(responseCode = "201")
    ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getPaymentById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listPayments")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<PaymentResponse>> listPayments(@ParameterObject Pageable pageable);
}
