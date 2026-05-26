package com.thiagsilvadev.erpapi.inventory.internal.dto.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryItemSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;

@Mapper(componentModel = "spring")
public abstract class InventoryItemMapper {

    protected CatalogFacade catalogFacade;

    @Autowired
    public void setCatalogFacade(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }

    @Mapping(target = "availableQuantity", expression = "java(item.availableQuantity())")
    @Mapping(target = "product", expression = "java(mapInventoryItemProduct(item.getProductId()))")
    public abstract InventoryItemResponse toResponse(InventoryItem item);

    protected InventoryItemProductResponse mapInventoryItemProduct(java.util.UUID productId) {
        return catalogFacade
                .findProductById(productId)
                .map(this::toInventoryItemProductResponse)
                .orElse(null);
    }

    protected InventoryItemProductResponse toInventoryItemProductResponse(ProductSnapshot product) {
        return new InventoryItemProductResponse(
                product.id(), product.sku(), product.name(), toInventoryItemCategoryResponse(product.category()));
    }

    protected InventoryItemCategoryResponse toInventoryItemCategoryResponse(CategorySnapshot category) {
        if (category == null) {
            return null;
        }
        return new InventoryItemCategoryResponse(category.id(), category.code(), category.name());
    }

    @Mapping(target = "warehouseId", source = "warehouse.id")
    public abstract InventoryItemSnapshot toSnapshot(InventoryItem item);
}
