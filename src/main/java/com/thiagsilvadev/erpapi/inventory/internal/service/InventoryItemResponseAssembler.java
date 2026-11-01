package com.thiagsilvadev.erpapi.inventory.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemCategoryResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemProductResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.mapper.WarehouseMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;

/**
 * Builds inventory item responses. It resolves the product snapshot through the catalog contract, so
 * it is a response assembler rather than a mapper and it must not be used to map collections without
 * batching the product lookups.
 */
@Component
public class InventoryItemResponseAssembler {

    private final CatalogFacade catalogFacade;

    public InventoryItemResponseAssembler(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }

    public InventoryItemResponse toResponse(InventoryItem item) {
        WarehouseResponse warehouse = WarehouseMapper.toResponse(item.getWarehouse());

        return new InventoryItemResponse(
                item.getId(),
                toProduct(item.getCompanyId(), item.getProductId()),
                warehouse,
                item.getQuantityOnHand(),
                item.getReservedQuantity(),
                item.availableQuantity());
    }

    private InventoryItemProductResponse toProduct(UUID companyId, UUID productId) {
        ProductSnapshot product = catalogFacade.getProduct(companyId, productId);
        return new InventoryItemProductResponse(
                product.id(), product.sku(), product.name(), toCategory(product.category()));
    }

    private static InventoryItemCategoryResponse toCategory(CategorySnapshot category) {
        return category == null
                ? null
                : new InventoryItemCategoryResponse(category.id(), category.code(), category.name());
    }
}
