package com.thiagsilvadev.erpapi.catalog.internal.usecase.product;

import static com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductSearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductSpecification;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

@Component
public class ProductQueryUseCase {

    private final ProductRepository productRepository;
    private final CatalogMapper catalogMapper;

    public ProductQueryUseCase(ProductRepository productRepository, CatalogMapper catalogMapper) {
        this.productRepository = productRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public ProductResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        Product product = productRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, id));

        return catalogMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<ProductResponse> list(ProductSearchCriteria criteria, Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return productRepository
                .findAll(ProductSpecification.withCriteriaForCompany(criteria, companyId), pageable)
                .map(catalogMapper::toResponse);
    }
}
