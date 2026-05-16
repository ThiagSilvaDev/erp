package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "financial")
public interface FinancialApi {

    @PostMapping("/invoices")
    @Operation(operationId = "createInvoice")
    @ApiResponse(responseCode = "201")
    ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request);

    @GetMapping("/invoices/{id}")
    @Operation(operationId = "getInvoiceById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID id);

    @GetMapping("/invoices")
    @Operation(operationId = "listInvoices")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<InvoiceResponse>> listInvoices(@ParameterObject Pageable pageable);

    @PostMapping(value = "/payments", consumes = "application/json")
    @Operation(operationId = "createPayment")
    @ApiResponse(responseCode = "201")
    ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request);

    @GetMapping("/payments/{id}")
    @Operation(operationId = "getPaymentById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id);

    @GetMapping("/payments")
    @Operation(operationId = "listPayments")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<PaymentResponse>> listPayments(@ParameterObject Pageable pageable);

    @GetMapping("/entries/{id}")
    @Operation(operationId = "getFinancialEntryById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<FinancialEntryResponse> getEntry(@PathVariable UUID id);

    @GetMapping("/entries")
    @Operation(operationId = "listFinancialEntries")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<FinancialEntryResponse>> listEntries(@ParameterObject Pageable pageable);
}
