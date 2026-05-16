package com.thiagsilvadev.erpapi.catalog.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SkuTest {

    @Test
    void shouldNormalizeSku() {
        Sku sku = Sku.create(" sku-1 ");

        assertThat(sku.getValue()).isEqualTo("SKU-1");
    }

    @Test
    void shouldRejectBlankSku() {
        assertThatThrownBy(() -> Sku.create(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product SKU cannot be blank");
    }
}
