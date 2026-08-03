package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceCriteria;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntrySearchCriteria;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.CreateAccountingEntryRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/accounting-entries", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "financial accounting entries")
interface AccountingEntryApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createAccountingEntry")
    @ApiResponse(responseCode = "201")
    ResponseEntity<AccountingEntryResponse> createEntry(@Valid @RequestBody CreateAccountingEntryRequest request);

    @GetMapping
    @Operation(operationId = "listAccountingEntries")
    @ApiResponse(responseCode = "200")
    Page<AccountingEntryResponse> listEntries(
            @ParameterObject AccountingEntrySearchCriteria criteria, @ParameterObject Pageable pageable);

    @GetMapping("/balances")
    @Operation(operationId = "listAccountBalances")
    @ApiResponse(responseCode = "200")
    List<AccountBalanceResponse> listBalances(@Valid @ParameterObject AccountBalanceCriteria criteria);
}
