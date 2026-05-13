package com.thiagsilvadev.erpapi.catalog.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thiagsilvadev.erpapi.catalog.internal.dto.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateCategoryRequest;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.CreateCategoryUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.GetCategoryByIdUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.ListCategoriesUseCase;

@RestController
@RequestMapping("/api/v1/catalog/categories")
@Validated
class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoryByIdUseCase getCategoryByIdUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;

    CategoryController(
            CreateCategoryUseCase createCategoryUseCase,
            GetCategoryByIdUseCase getCategoryByIdUseCase,
            ListCategoriesUseCase listCategoriesUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.getCategoryByIdUseCase = getCategoryByIdUseCase;
        this.listCategoriesUseCase = listCategoriesUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = createCategoryUseCase.execute(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(getCategoryByIdUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> listCategories(CategorySearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(listCategoriesUseCase.execute(criteria, pageable));
    }
}
