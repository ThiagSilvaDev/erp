package com.thiagsilvadev.erpapi.company.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

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

@RequestMapping(value = "/api/v1/company", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "companies", description = "Company management endpoints")
public interface CompanyApi {

    @PostMapping
    @Operation(operationId = "createCompany", summary = "Create a new company")
    @ApiResponse(responseCode = "201", description = "Company created successfully")
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getCompanyById", summary = "Get a company by ID")
    @ApiResponse(responseCode = "200", description = "Company retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Company not found")
    ResponseEntity<CompanyResponse> getCompanyById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listCompanies", summary = "List companies")
    @ApiResponse(responseCode = "200", description = "Companies listed successfully")
    ResponseEntity<Page<CompanyResponse>> listCompanies(CompanySearchCriteria criteria, Pageable pageable);
}
