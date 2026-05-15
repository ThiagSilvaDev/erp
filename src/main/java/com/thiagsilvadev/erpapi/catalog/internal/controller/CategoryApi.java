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

import com.thiagsilvadev.erpapi.catalog.internal.dto.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateCategoryRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/catalog/categories", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "catalog", description = "Category management endpoints")
public interface CategoryApi {

    @PostMapping
    @Operation(operationId = "createCategory", summary = "Create a new category")
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getCategoryById", summary = "Get a category by ID")
    @ApiResponse(responseCode = "200", description = "Category retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listCategories", summary = "List categories")
    @ApiResponse(responseCode = "200", description = "Categories listed successfully")
    ResponseEntity<Page<CategoryResponse>> listCategories(CategorySearchCriteria criteria, Pageable pageable);
}
