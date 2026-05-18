package com.thiagsilvadev.erpapi.catalog.internal.usecase.category;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CreateCategoryRequest;
import com.thiagsilvadev.erpapi.catalog.internal.exception.CategoryCodeAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;

@Component
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;

    public CreateCategoryUseCase(CategoryRepository categoryRepository, CatalogMapper catalogMapper) {
        this.categoryRepository = categoryRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions).WRITE)")
    public CategoryResponse execute(CreateCategoryRequest request) {
        String code = Category.normalizeCode(request.code());

        if (categoryRepository.existsByCode(code)) {
            throw new CategoryCodeAlreadyExistsException(code);
        }

        Category category = Category.create(request.code(), request.name(), request.description());
        Category savedCategory;
        try {
            savedCategory = categoryRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException e) {
            throw new CategoryCodeAlreadyExistsException(code);
        }

        return catalogMapper.toResponse(savedCategory);
    }
}
