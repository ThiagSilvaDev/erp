package com.thiagsilvadev.erpapi.catalog.internal.mapper;

import org.mapstruct.Mapper;

import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateCategoryRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CreateProductRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.common.domain.model.Money;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    CategoryResponse toResponse(Category category);

    ProductResponse toResponse(Product product);

    CategorySnapshot toSnapshot(Category category);

    ProductSnapshot toSnapshot(Product product);

    MoneyResponse toResponse(Money money);

    default Category toEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }
        return Category.create(request.code(), request.name(), request.description());
    }

    default Product toEntity(CreateProductRequest request, Category category) {
        if (request == null) {
            return null;
        }
        return Product.create(
                request.sku(), request.name(), request.description(), toMoney(request.basePrice()), category);
    }

    default Money toMoney(MoneyRequest request) {
        if (request == null) {
            return null;
        }
        return Money.of(request.amount(), request.currencyCode());
    }
}
