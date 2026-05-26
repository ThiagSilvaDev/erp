package com.thiagsilvadev.erpapi.catalog.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.catalog.internal.exception.ProductErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class SkuTest {

    @Test
    void shouldNormalizeSku() {
        Sku sku = Sku.of(" sku-1 ");

        assertThat(sku.getValue()).isEqualTo("SKU-1");
    }

    @Test
    void shouldRejectBlankSku() {
        String value = " ";

        var exception = catchThrowableOfType(DomainValidationException.class, () -> Sku.of(value));

        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.SKU_REQUIRED);
    }
}
