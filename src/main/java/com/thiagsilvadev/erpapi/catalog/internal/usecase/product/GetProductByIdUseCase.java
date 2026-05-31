package com.thiagsilvadev.erpapi.catalog.internal.usecase.product;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

@Component
public class GetProductByIdUseCase {

    private final ProductRepository productRepository;
    private final CatalogMapper catalogMapper;

    public GetProductByIdUseCase(ProductRepository productRepository, CatalogMapper catalogMapper) {
        this.productRepository = productRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions).READ)")
    public ProductResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        Product product = productRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, id));

        return catalogMapper.toResponse(product);
    }
}
