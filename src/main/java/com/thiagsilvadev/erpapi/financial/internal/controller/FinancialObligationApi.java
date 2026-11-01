package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.financialobligation.FinancialObligationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/financial/financial-obligations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "financial obligations")
interface FinancialObligationApi {

    @GetMapping("/{id}")
    @Operation(operationId = "getFinancialObligationById")
    @ApiResponse(responseCode = "200")
    FinancialObligationResponse getFinancialObligation(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listFinancialObligations")
    @ApiResponse(responseCode = "200")
    Page<FinancialObligationResponse> listFinancialObligations(@ParameterObject Pageable pageable);
}
