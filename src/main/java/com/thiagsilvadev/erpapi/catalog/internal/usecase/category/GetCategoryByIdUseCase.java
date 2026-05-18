package com.thiagsilvadev.erpapi.catalog.internal.usecase.category;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;

@Component
public class GetCategoryByIdUseCase {

    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;

    public GetCategoryByIdUseCase(CategoryRepository categoryRepository, CatalogMapper catalogMapper) {
        this.categoryRepository = categoryRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions).READ)")
    public CategoryResponse execute(UUID id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Category.class, id));

        return catalogMapper.toResponse(category);
    }
}
