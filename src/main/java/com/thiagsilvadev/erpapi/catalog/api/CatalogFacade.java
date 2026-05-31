package com.thiagsilvadev.erpapi.catalog.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CatalogFacade {

    Optional<ProductSnapshot> findProductById(UUID id);

    Optional<ProductSnapshot> findProductById(UUID id, UUID companyId);

    Optional<ProductSnapshot> findProductBySku(String sku);

    Optional<ProductSnapshot> findProductBySku(String sku, UUID companyId);

    void requireProductExistsById(UUID id);

    void requireProductExistsById(UUID id, UUID companyId);

    List<ProductSnapshot> findAllProductsByIdIn(Set<UUID> productIds);

    List<ProductSnapshot> findAllProductsByIdIn(Set<UUID> productIds, UUID companyId);

    Optional<CategorySnapshot> findCategoryById(UUID id);

    Optional<CategorySnapshot> findCategoryByCode(String code);
}
