package com.thiagsilvadev.erpapi.catalog.internal.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateCategoryRequest;
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
    public CategoryResponse execute(CreateCategoryRequest request) {
        String code = Category.normalizeCode(request.code());

        if (categoryRepository.existsByCode(code)) {
            throw new CategoryCodeAlreadyExistsException(code);
        }

        Category category = Category.create(request.code(), request.name(), request.description());
        Category savedCategory = categoryRepository.save(category);

        return catalogMapper.toResponse(savedCategory);
    }
}
