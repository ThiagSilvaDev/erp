package com.thiagsilvadev.erpapi.catalog.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;

@Service
public class CatalogService implements CatalogFacade {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CatalogMapper catalogMapper;

    public CatalogService(
            ProductRepository productRepository, CategoryRepository categoryRepository, CatalogMapper catalogMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.catalogMapper = catalogMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findProductById(UUID id) {
        return productRepository.findById(id).map(catalogMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findProductBySku(String sku) {
        return productRepository.findBySku(Product.normalizeSku(sku)).map(catalogMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategorySnapshot> findCategoryById(UUID id) {
        return categoryRepository.findById(id).map(catalogMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategorySnapshot> findCategoryByCode(String code) {
        return categoryRepository.findByCode(Category.normalizeCode(code)).map(catalogMapper::toSnapshot);
    }
}
