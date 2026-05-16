package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialEntryResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/entries", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "financial entries")
interface FinancialEntryApi {

    @GetMapping("/{id}")
    @Operation(operationId = "getFinancialEntryById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<FinancialEntryResponse> getEntry(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listFinancialEntries")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<FinancialEntryResponse>> listEntries(@ParameterObject Pageable pageable);
}
