package com.thiagsilvadev.erpapi.financial.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.CreateInvoiceRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.invoice.InvoiceSearchCriteria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "financial-invoices")
interface InvoiceApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createInvoice")
    @ApiResponse(responseCode = "201", description = "Invoice created")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ResponseEntity<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getInvoiceById")
    @ApiResponse(responseCode = "200", description = "Invoice retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    InvoiceResponse getById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listInvoices")
    @ApiResponse(responseCode = "200", description = "Invoices retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    Page<InvoiceResponse> list(
            @Valid @ParameterObject InvoiceSearchCriteria criteria, @ParameterObject Pageable pageable);
}
