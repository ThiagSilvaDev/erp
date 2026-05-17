package com.thiagsilvadev.erpapi.catalog.internal.usecase.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategorySpecification;

@Component
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;

    public ListCategoriesUseCase(CategoryRepository categoryRepository, CatalogMapper catalogMapper) {
        this.categoryRepository = categoryRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions).READ)")
    public Page<CategoryResponse> execute(CategorySearchCriteria criteria, Pageable pageable) {
        return categoryRepository
                .findAll(CategorySpecification.withCriteria(criteria), pageable)
                .map(catalogMapper::toResponse);
    }
}
