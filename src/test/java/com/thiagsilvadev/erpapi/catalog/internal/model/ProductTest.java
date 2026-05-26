package com.thiagsilvadev.erpapi.catalog.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.catalog.internal.exception.ProductErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.common.domain.model.Money;

class ProductTest {

    @Test
    void shouldNormalizeSkuNameAndDescription() {
        Product product = Product.create(" sku-001 ", " Product ", "  ", money(), category());

        assertThat(product.getSku()).isEqualTo("SKU-001");
        assertThat(product.getName()).isEqualTo("Product");
        assertThat(product.getDescription()).isNull();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void shouldRejectRequiredSku(String sku) {
        String name = "Product";
        Money basePrice = money();
        Category category = category();

        var exception = catchThrowableOfType(
                DomainValidationException.class, () -> Product.create(sku, name, null, basePrice, category));

        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.SKU_REQUIRED);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void shouldRejectRequiredNameOnCreate(String name) {
        String sku = "SKU-001";
        Money basePrice = money();
        Category category = category();

        var exception = catchThrowableOfType(
                DomainValidationException.class, () -> Product.create(sku, name, null, basePrice, category));

        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.NAME_REQUIRED);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void shouldRejectRequiredNameOnUpdate(String name) {
        Product product = product();
        Money basePrice = money();
        Category category = category();

        var exception = catchThrowableOfType(
                DomainValidationException.class, () -> product.update(name, null, basePrice, category));

        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.NAME_REQUIRED);
    }

    @Test
    void shouldRejectRequiredBasePriceOnCreate() {
        String sku = "SKU-001";
        String name = "Product";
        Category category = category();

        var exception = catchThrowableOfType(
                DomainValidationException.class, () -> Product.create(sku, name, null, null, category));

        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.BASE_PRICE_REQUIRED);
    }

    @Test
    void shouldRejectRequiredBasePriceOnUpdate() {
        Product product = product();
        Category category = category();

        var exception = catchThrowableOfType(
                DomainValidationException.class, () -> product.update("Product", null, null, category));

        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.BASE_PRICE_REQUIRED);
    }

    @Test
    void shouldRejectRequiredCategoryOnCreate() {
        String sku = "SKU-001";
        String name = "Product";
        Money basePrice = money();

        var exception = catchThrowableOfType(
                DomainValidationException.class, () -> Product.create(sku, name, null, basePrice, null));

        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.CATEGORY_REQUIRED);
    }

    @Test
    void shouldRejectRequiredCategoryOnUpdate() {
        Product product = product();
        Money basePrice = money();

        var exception = catchThrowableOfType(
                DomainValidationException.class, () -> product.update("Product", null, basePrice, null));

        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.CATEGORY_REQUIRED);
    }

    private static Product product() {
        return Product.create("SKU-001", "Product", null, money(), category());
    }

    private static Money money() {
        return Money.reais(new BigDecimal("10.00"));
    }

    private static Category category() {
        return Category.create("RAW", "Raw materials", null);
    }
}
