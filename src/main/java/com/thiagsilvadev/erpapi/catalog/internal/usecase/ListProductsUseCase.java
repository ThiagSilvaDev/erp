package com.thiagsilvadev.erpapi.catalog.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductSearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductSpecification;

@Component
public class ListProductsUseCase {

    private final ProductRepository productRepository;
    private final CatalogMapper catalogMapper;

    public ListProductsUseCase(ProductRepository productRepository, CatalogMapper catalogMapper) {
        this.productRepository = productRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> execute(ProductSearchCriteria criteria, Pageable pageable) {
        return productRepository
                .findAll(ProductSpecification.withCriteria(criteria), pageable)
                .map(catalogMapper::toResponse);
    }
}
