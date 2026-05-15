package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "financial", description = "Operational finance endpoints")
public interface FinancialApi {

    @PostMapping("/invoices")
    @Operation(operationId = "createInvoice", summary = "Create an invoice")
    @ApiResponse(responseCode = "201", description = "Invoice created successfully")
    ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request);

    @GetMapping("/invoices/{id}")
    @Operation(operationId = "getInvoiceById", summary = "Get an invoice by ID")
    @ApiResponse(responseCode = "200", description = "Invoice retrieved successfully")
    InvoiceResponse getInvoice(@PathVariable UUID id);

    @GetMapping("/invoices")
    @Operation(operationId = "listInvoices", summary = "List invoices")
    @ApiResponse(responseCode = "200", description = "Invoices listed successfully")
    Page<InvoiceResponse> listInvoices(Pageable pageable);

    @PostMapping("/payments")
    @Operation(operationId = "createPayment", summary = "Create a payment")
    @ApiResponse(responseCode = "201", description = "Payment created successfully")
    ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request);

    @GetMapping("/payments/{id}")
    @Operation(operationId = "getPaymentById", summary = "Get a payment by ID")
    @ApiResponse(responseCode = "200", description = "Payment retrieved successfully")
    PaymentResponse getPayment(@PathVariable UUID id);

    @GetMapping("/payments")
    @Operation(operationId = "listPayments", summary = "List payments")
    @ApiResponse(responseCode = "200", description = "Payments listed successfully")
    Page<PaymentResponse> listPayments(Pageable pageable);

    @GetMapping("/entries/{id}")
    @Operation(operationId = "getFinancialEntryById", summary = "Get a financial entry by ID")
    @ApiResponse(responseCode = "200", description = "Financial entry retrieved successfully")
    FinancialEntryResponse getEntry(@PathVariable UUID id);

    @GetMapping("/entries")
    @Operation(operationId = "listFinancialEntries", summary = "List financial entries")
    @ApiResponse(responseCode = "200", description = "Financial entries listed successfully")
    Page<FinancialEntryResponse> listEntries(Pageable pageable);
}
