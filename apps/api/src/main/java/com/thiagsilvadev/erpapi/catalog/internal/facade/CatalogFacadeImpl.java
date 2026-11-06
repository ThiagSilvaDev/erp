package com.thiagsilvadev.erpapi.catalog.internal.facade;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;

@Service
public class CatalogFacadeImpl implements CatalogFacade {

    private final ProductRepository productRepository;
    private final CatalogMapper catalogMapper;

    public CatalogFacadeImpl(ProductRepository productRepository, CatalogMapper catalogMapper) {
        this.productRepository = productRepository;
        this.catalogMapper = catalogMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSnapshot getProduct(UUID companyId, UUID productId) {
        return productRepository
                .findByIdAndCompanyId(productId, companyId)
                .map(catalogMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, productId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSnapshot resolveProduct(UUID companyId, String sku) {
        return productRepository
                .findByCompanyIdAndSkuValue(companyId, Sku.of(sku).getValue())
                .map(catalogMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, sku));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSnapshot> getProducts(UUID companyId, Set<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Product> products = productRepository.findAllByCompanyIdAndIdIn(companyId, productIds);
        Set<UUID> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        Set<UUID> missingIds = new TreeSet<>(productIds);
        missingIds.removeAll(foundIds);
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException(Product.class, missingIds);
        }
        return products.stream().map(catalogMapper::toSnapshot).toList();
    }
}
