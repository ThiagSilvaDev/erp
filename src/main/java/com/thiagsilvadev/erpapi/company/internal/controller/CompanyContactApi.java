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
import org.springframework.web.bind.annotation.RequestParam;

import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyContactRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/company-contacts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "company-contacts")
public interface CompanyContactApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createCompanyContact")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "404")
    ResponseEntity<CompanyContactResponse> createCompanyContact(
            @Valid @RequestBody CreateCompanyContactRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getCompanyContactById")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404")
    CompanyContactResponse getCompanyContactById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listCompanyContacts")
    @ApiResponse(responseCode = "200")
    Page<CompanyContactResponse> listCompanyContacts(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) Boolean primaryContact,
            @ParameterObject Pageable pageable);

    @PostMapping("/{id}/primary")
    @Operation(operationId = "markCompanyContactPrimary")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404")
    CompanyContactResponse markCompanyContactPrimary(@PathVariable UUID id);
}
