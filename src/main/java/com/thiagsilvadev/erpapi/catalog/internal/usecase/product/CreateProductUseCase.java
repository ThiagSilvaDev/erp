package com.thiagsilvadev.erpapi.catalog.internal.usecase.product;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.product.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.event.CatalogEventPublisher;
import com.thiagsilvadev.erpapi.catalog.internal.exception.SkuAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Money;

@Component
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;
    private final CatalogEventPublisher eventPublisher;

    public CreateProductUseCase(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            CatalogMapper catalogMapper,
            CatalogEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.catalogMapper = catalogMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions).WRITE)")
    public ProductResponse execute(CreateProductRequest request) {
        Sku sku = Sku.of(request.sku());

        if (productRepository.existsBySkuValue(sku.getValue())) {
            throw new SkuAlreadyExistsException(sku.getValue());
        }

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, request.categoryId()));
        Money basePrice =
                Money.of(request.basePrice().amount(), request.basePrice().currencyCode());
        Product product = Product.create(sku, request.name(), request.description(), basePrice, category);

        Product savedProduct;
        try {
            savedProduct = productRepository.saveAndFlush(product);
        } catch (DataIntegrityViolationException e) {
            throw new SkuAlreadyExistsException(sku.getValue());
        }
        eventPublisher.productCreated(savedProduct);

        return catalogMapper.toResponse(savedProduct);
    }
}
