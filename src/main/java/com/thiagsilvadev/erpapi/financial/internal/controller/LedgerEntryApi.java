package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.ledgerentry.LedgerEntryResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/ledger-entries", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "ledger entries")
interface LedgerEntryApi {

    @GetMapping("/{id}")
    @Operation(operationId = "getLedgerEntryById")
    @ApiResponse(responseCode = "200")
    LedgerEntryResponse getLedgerEntry(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listLedgerEntries")
    @ApiResponse(responseCode = "200")
    Page<LedgerEntryResponse> listLedgerEntries(@ParameterObject Pageable pageable);
}
