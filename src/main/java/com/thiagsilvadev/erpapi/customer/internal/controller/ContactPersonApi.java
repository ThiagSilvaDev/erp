package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.customer.internal.dto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/contact-persons", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "contact-persons", description = "Contact person management endpoints")
public interface ContactPersonApi {

    @PostMapping
    @Operation(operationId = "createContactPerson", summary = "Create a contact person")
    @ApiResponse(responseCode = "201", description = "Contact person created successfully")
    ResponseEntity<ContactPersonResponse> createContactPerson(@Valid @RequestBody CreateContactPersonRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getContactPersonById", summary = "Get a contact person by ID")
    @ApiResponse(responseCode = "200", description = "Contact person retrieved successfully")
    ResponseEntity<ContactPersonResponse> getContactPersonById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listContactPersons", summary = "List contact persons")
    @ApiResponse(responseCode = "200", description = "Contact persons listed successfully")
    ResponseEntity<Page<ContactPersonResponse>> listContactPersons(Pageable pageable);
}
