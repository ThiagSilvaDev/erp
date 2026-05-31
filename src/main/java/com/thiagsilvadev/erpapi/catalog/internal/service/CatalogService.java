package com.thiagsilvadev.erpapi.catalog.internal.service;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;

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
    public Optional<ProductSnapshot> findProductById(UUID id, UUID companyId) {
        return productRepository.findByIdAndCompanyId(id, companyId).map(catalogMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findProductBySku(String sku) {
        return productRepository.findBySkuValue(Sku.of(sku).getValue()).map(catalogMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findProductBySku(String sku, UUID companyId) {
        return productRepository
                .findByCompanyIdAndSkuValue(companyId, Sku.of(sku).getValue())
                .map(catalogMapper::toSnapshot);
    }

    @Override
    public void requireProductExistsById(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(Product.class, id);
        }
    }

    @Override
    public void requireProductExistsById(UUID id, UUID companyId) {
        if (productRepository.findByIdAndCompanyId(id, companyId).isEmpty()) {
            throw new ResourceNotFoundException(Product.class, id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSnapshot> findAllProductsByIdIn(Set<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Product> products = productRepository.findAllByIdIn((productIds));

        return products.stream().map(catalogMapper::toSnapshot).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSnapshot> findAllProductsByIdIn(Set<UUID> productIds, UUID companyId) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Product> products = productRepository.findAllByCompanyIdAndIdIn(companyId, productIds);

        return products.stream().map(catalogMapper::toSnapshot).toList();
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
