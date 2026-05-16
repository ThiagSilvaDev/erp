package com.thiagsilvadev.erpapi.catalog.internal.mapper;

import org.mapstruct.Mapper;

import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyRequest;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.common.domain.model.Money;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    CategoryResponse toResponse(Category category);

    ProductResponse toResponse(Product product);

    CategorySnapshot toSnapshot(Category category);

    ProductSnapshot toSnapshot(Product product);

    MoneyResponse toResponse(Money money);

    default Money toMoney(MoneyRequest request) {
        if (request == null) {
            return null;
        }
        return Money.of(request.amount(), request.currencyCode());
    }

    default Sku toSku(String value) {
        return value == null ? null : Sku.create(value);
    }
}
