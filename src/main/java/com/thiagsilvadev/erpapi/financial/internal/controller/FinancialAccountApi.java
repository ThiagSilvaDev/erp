package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreateFinancialAccountRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialEntryResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "financial accounts")
interface FinancialAccountApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createFinancialAccount")
    @ApiResponse(responseCode = "201")
    ResponseEntity<FinancialAccountResponse> createAccount(@Valid @RequestBody CreateFinancialAccountRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getFinancialAccountById")
    @ApiResponse(responseCode = "200")
    FinancialAccountResponse getAccount(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listFinancialAccounts")
    @ApiResponse(responseCode = "200")
    Page<FinancialAccountResponse> listAccounts(@ParameterObject Pageable pageable);

    @GetMapping("/{accountId}/entries")
    @Operation(operationId = "listFinancialEntries")
    @ApiResponse(responseCode = "200")
    Page<FinancialEntryResponse> listEntries(@PathVariable UUID accountId, @ParameterObject Pageable pageable);
}
