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

import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CreateCategoryRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/api/v1/catalog/categories", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "catalog")
public interface CategoryApi {

    @PostMapping(consumes = "application/json")
    @Operation(operationId = "createCategory")
    @ApiResponse(responseCode = "201")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "422")
    ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getCategoryById")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404")
    CategoryResponse getCategoryById(@PathVariable UUID id);

    @GetMapping
    @Operation(operationId = "listCategories")
    @ApiResponse(responseCode = "200")
    Page<CategoryResponse> listCategories(
            @ParameterObject CategorySearchCriteria criteria, @ParameterObject Pageable pageable);
}
