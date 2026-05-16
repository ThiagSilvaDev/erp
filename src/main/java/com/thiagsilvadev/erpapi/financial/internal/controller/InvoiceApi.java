package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.InvoiceResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "financial invoices")
interface InvoiceApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createInvoice")
    @ApiResponse(responseCode = "201")
    ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getInvoiceById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listInvoices")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<InvoiceResponse>> listInvoices(@ParameterObject Pageable pageable);
}
