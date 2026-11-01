package com.thiagsilvadev.erpapi.catalog.internal.usecase.category;

import static com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategorySpecification;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

@Component
public class CategoryQueryUseCase {

    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;

    public CategoryQueryUseCase(CategoryRepository categoryRepository, CatalogMapper catalogMapper) {
        this.categoryRepository = categoryRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public CategoryResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        Category category = categoryRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, id));

        return catalogMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<CategoryResponse> list(CategorySearchCriteria criteria, Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return categoryRepository
                .findAll(CategorySpecification.withCriteriaForCompany(criteria, companyId), pageable)
                .map(catalogMapper::toResponse);
    }
}
