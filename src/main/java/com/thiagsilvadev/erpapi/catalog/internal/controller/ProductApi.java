package com.thiagsilvadev.erpapi.catalog.internal.controller;

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

import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductSearchCriteria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/catalog/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "catalog", description = "Product management endpoints")
public interface ProductApi {

    @PostMapping
    @Operation(operationId = "createProduct", summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getProductById", summary = "Get a product by ID")
    @ApiResponse(responseCode = "200", description = "Product retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listProducts", summary = "List products")
    @ApiResponse(responseCode = "200", description = "Products listed successfully")
    ResponseEntity<Page<ProductResponse>> listProducts(ProductSearchCriteria criteria, Pageable pageable);
}
