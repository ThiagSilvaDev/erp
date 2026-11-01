package com.thiagsilvadev.erpapi.inventory.internal.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemCategoryResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemProductResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;

@Mapper(componentModel = "spring")
public abstract class InventoryItemMapper {

    protected CatalogFacade catalogFacade;

    @Autowired
    public void setCatalogFacade(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }

    @Mapping(target = "availableQuantity", expression = "java(item.availableQuantity())")
    @Mapping(target = "product", expression = "java(mapInventoryItemProduct(item.getCompanyId(), item.getProductId()))")
    public abstract InventoryItemResponse toResponse(InventoryItem item);

    protected InventoryItemProductResponse mapInventoryItemProduct(UUID companyId, UUID productId) {
        return toInventoryItemProductResponse(catalogFacade.getProduct(companyId, productId));
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
}
