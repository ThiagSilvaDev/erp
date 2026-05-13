package com.thiagsilvadev.erpapi.catalog.internal.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.exception.ResourceNotFoundException;

@Component
public class GetProductByIdUseCase {

    private final ProductRepository productRepository;
    private final CatalogMapper catalogMapper;

    public GetProductByIdUseCase(ProductRepository productRepository, CatalogMapper catalogMapper) {
        this.productRepository = productRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    public ProductResponse execute(UUID id) {
        Product product =
                productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Product.class, id));

        return catalogMapper.toResponse(product);
    }
}
