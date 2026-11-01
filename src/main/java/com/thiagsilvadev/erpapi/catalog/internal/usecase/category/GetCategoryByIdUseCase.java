package com.thiagsilvadev.erpapi.catalog.internal.usecase.category;

import static com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

@Component
public class GetCategoryByIdUseCase {

    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;

    public GetCategoryByIdUseCase(CategoryRepository categoryRepository, CatalogMapper catalogMapper) {
        this.categoryRepository = categoryRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public CategoryResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        Category category = categoryRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, id));

        return catalogMapper.toResponse(category);
    }
}
