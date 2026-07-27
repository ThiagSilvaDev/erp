package com.thiagsilvadev.erpapi.kpi.internal.controller;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.thiagsilvadev.erpapi.kpi.internal.dto.ExecutiveKpiResponse;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiCriteria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/kpis", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "kpis")
interface KpiApi {

    @GetMapping("/executive")
    @Operation(operationId = "getExecutiveKpis")
    @ApiResponse(responseCode = "200")
    ExecutiveKpiResponse getExecutiveKpis(@Valid @ParameterObject KpiCriteria criteria);
}
