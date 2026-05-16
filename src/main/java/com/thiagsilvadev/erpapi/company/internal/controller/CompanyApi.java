package com.thiagsilvadev.erpapi.company.internal.controller;

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

import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanySearchCriteria;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/companies", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "companies")
public interface CompanyApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createCompany")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "422")
    ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getCompanyById")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404")
    ResponseEntity<CompanyResponse> getCompanyById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listCompanies")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<CompanyResponse>> listCompanies(
            @ParameterObject CompanySearchCriteria criteria, @ParameterObject Pageable pageable);
}
