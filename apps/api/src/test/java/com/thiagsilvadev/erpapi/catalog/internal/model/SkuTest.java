package com.thiagsilvadev.erpapi.catalog.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.Locale;

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

    @Test
    void shouldEnforceLengthAfterNormalization() {
        assertThat(Sku.of("X".repeat(80)).getValue()).hasSize(80);
        String overlong = "X".repeat(81);
        assertThatThrownBy(() -> Sku.of(overlong)).isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldNormalizeIndependentlyOfDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(Sku.of("item").getValue()).isEqualTo("ITEM");
        } finally {
            Locale.setDefault(original);
        }
    }
}
