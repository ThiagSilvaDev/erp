package com.thiagsilvadev.erpapi.catalog.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CreateCategoryRequest;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.category.CreateCategoryUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.category.GetCategoryByIdUseCase;
import com.thiagsilvadev.erpapi.catalog.internal.usecase.category.ListCategoriesUseCase;
import com.thiagsilvadev.erpapi.common.web.ResponseEntities;

@RestController
@Validated
class CategoryController implements CategoryApi {

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

    @Override
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = createCategoryUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public CategoryResponse getCategoryById(@PathVariable UUID id) {
        return getCategoryByIdUseCase.execute(id);
    }

    @Override
    public Page<CategoryResponse> listCategories(CategorySearchCriteria criteria, Pageable pageable) {
        return listCategoriesUseCase.execute(criteria, pageable);
    }
}
