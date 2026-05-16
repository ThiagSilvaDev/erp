package com.thiagsilvadev.erpapi.catalog.internal.controller;

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

import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductSearchCriteria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/catalog/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "catalog")
public interface ProductApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createProduct")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "422")
    ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getProductById")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404")
    ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listProducts")
    @ApiResponse(responseCode = "200")
    ResponseEntity<Page<ProductResponse>> listProducts(
            @ParameterObject ProductSearchCriteria criteria, @ParameterObject Pageable pageable);
}
