package com.thiagsilvadev.erpapi.catalog.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.catalog.api.CategorySnapshot;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategoryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySummaryResponse;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductResponse;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.common.domain.model.Money;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    CategoryResponse toResponse(Category category);

    CategorySummaryResponse toSummaryResponse(Category category);

    @Mapping(target = "formattedBasePrice", expression = "java(formatBasePrice(product.getBasePrice()))")
    ProductResponse toResponse(Product product);

    CategorySnapshot toSnapshot(Category category);

    ProductSnapshot toSnapshot(Product product);

    MoneyResponse toResponse(Money money);

    default String formatBasePrice(Money money) {
        if (money == null) {
            return null;
        }
        return "%s %s".formatted(money.getCurrencyCode(), money.getAmount().toPlainString());
    }
}
