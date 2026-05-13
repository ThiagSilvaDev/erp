package com.thiagsilvadev.erpapi.catalog.api;

import java.util.Optional;
import java.util.UUID;

public interface CatalogFacade {

    Optional<ProductSnapshot> findProductById(UUID id);

    Optional<ProductSnapshot> findProductBySku(String sku);

    Optional<CategorySnapshot> findCategoryById(UUID id);

    Optional<CategorySnapshot> findCategoryByCode(String code);
}
