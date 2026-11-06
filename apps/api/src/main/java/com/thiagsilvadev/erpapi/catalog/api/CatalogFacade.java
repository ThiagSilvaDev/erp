package com.thiagsilvadev.erpapi.catalog.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CatalogFacade {

    ProductSnapshot getProduct(UUID companyId, UUID productId);

    ProductSnapshot resolveProduct(UUID companyId, String sku);

    List<ProductSnapshot> getProducts(UUID companyId, Set<UUID> productIds);
}
