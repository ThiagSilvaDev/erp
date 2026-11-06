package com.thiagsilvadev.erpapi.financial.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.CONFLICT;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNPROCESSABLE_ENTITY;

import java.util.List;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceCriteria;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntrySearchCriteria;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.CreateAccountingEntryRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/accounting-entries", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "financial-accounting-entries")
interface AccountingEntryApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createAccountingEntry")
    @ApiResponse(responseCode = "201", description = "Accounting entry created")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = CONFLICT)
    ResponseEntity<AccountingEntryResponse> create(@Valid @RequestBody CreateAccountingEntryRequest request);

    @GetMapping
    @Operation(operationId = "listAccountingEntries")
    @ApiResponse(responseCode = "200", description = "Accounting entries retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    Page<AccountingEntryResponse> list(
            @Valid @ParameterObject AccountingEntrySearchCriteria criteria, @ParameterObject Pageable pageable);

    @GetMapping("/balances")
    @Operation(operationId = "listAccountBalances")
    @ApiResponse(responseCode = "200", description = "Account balances retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    List<AccountBalanceResponse> listBalances(@Valid @ParameterObject AccountBalanceCriteria criteria);
}
