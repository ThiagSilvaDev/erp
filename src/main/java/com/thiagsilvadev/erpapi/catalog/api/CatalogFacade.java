package com.thiagsilvadev.erpapi.catalog.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CatalogFacade {

    Optional<ProductSnapshot> findProductById(UUID id);

    Optional<ProductSnapshot> findProductBySku(String sku);

    List<ProductSnapshot> findAllProductsByIdIn(Set<UUID> productIds);

    Optional<CategorySnapshot> findCategoryById(UUID id);

    Optional<CategorySnapshot> findCategoryByCode(String code);
}
