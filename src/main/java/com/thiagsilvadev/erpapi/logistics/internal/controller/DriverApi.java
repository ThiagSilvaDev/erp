package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CreateDriverRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.DriverResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/logistics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "logistics")
public interface DriverApi {

    @PostMapping(value = "/drivers", consumes = "application/json")
    @Operation(operationId = "createDriver")
    @ApiResponse(responseCode = "201")
    ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody CreateDriverRequest request);

    @GetMapping("/drivers/{id}")
    @Operation(operationId = "getDriverById")
    @ApiResponse(responseCode = "200")
    DriverResponse getDriver(@PathVariable UUID id);

    @GetMapping("/drivers")
    @Operation(operationId = "listDrivers")
    @ApiResponse(responseCode = "200")
    Page<DriverResponse> listDrivers(@ParameterObject Pageable pageable);
}
