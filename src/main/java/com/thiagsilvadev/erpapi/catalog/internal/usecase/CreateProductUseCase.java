package com.thiagsilvadev.erpapi.catalog.internal.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.event.CatalogEventPublisher;
import com.thiagsilvadev.erpapi.catalog.internal.exception.SkuAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;

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
    public ProductResponse execute(CreateProductRequest request) {
        Sku sku = catalogMapper.toSku(request.sku());

        if (productRepository.existsBySkuValue(sku.getValue())) {
            throw new SkuAlreadyExistsException(sku.getValue());
        }

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, request.categoryId()));
        Product product = Product.create(
                sku, request.name(), request.description(), catalogMapper.toMoney(request.basePrice()), category);
        Product savedProduct = productRepository.save(product);

        eventPublisher.productCreated(savedProduct);

        return catalogMapper.toResponse(savedProduct);
    }
}
