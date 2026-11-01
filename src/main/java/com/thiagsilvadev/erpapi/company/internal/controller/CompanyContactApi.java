package com.thiagsilvadev.erpapi.company.internal.controller;

import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.BAD_REQUEST;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.FORBIDDEN;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.NOT_FOUND;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNAUTHORIZED;
import static com.thiagsilvadev.erpapi.common.web.openapi.ApiResponseRefs.UNPROCESSABLE_ENTITY;

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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/company-contacts", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "company-contacts")
public interface CompanyContactApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createCompanyContact")
    @ApiResponse(responseCode = "201", description = "Company contact created")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    @ApiResponse(responseCode = "422", ref = UNPROCESSABLE_ENTITY)
    ResponseEntity<CompanyContactResponse> createCompanyContact(
            @Valid @RequestBody CreateCompanyContactRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getCompanyContactById")
    @ApiResponse(responseCode = "200", description = "Company contact retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    CompanyContactResponse getCompanyContactById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listCompanyContacts")
    @ApiResponse(responseCode = "200", description = "Company contacts retrieved")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    Page<CompanyContactResponse> listCompanyContacts(
            @RequestParam(required = false) Boolean primaryContact, @ParameterObject Pageable pageable);

    @PostMapping("/{id}/primary")
    @Operation(operationId = "markCompanyContactPrimary")
    @ApiResponse(responseCode = "200", description = "Primary company contact updated")
    @ApiResponse(responseCode = "400", ref = BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = NOT_FOUND)
    CompanyContactResponse markCompanyContactPrimary(@PathVariable UUID id);
}
