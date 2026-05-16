package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "contact-persons")
public interface ContactPersonApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createContactPerson")
    @ApiResponse(responseCode = "201")
    ResponseEntity<ContactPersonResponse> createContactPerson(@Valid @RequestBody CreateContactPersonRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getContactPersonById")
    @ApiResponse(responseCode = "200")
    ResponseEntity<ContactPersonResponse> getContactPersonById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listContactPersons")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<ContactPersonResponse>> listContactPersons(@ParameterObject Pageable pageable);
}
