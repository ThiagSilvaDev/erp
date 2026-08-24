package com.thiagsilvadev.erpapi.common.domain.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class StringsTest {

    @Test
    void shouldTrimBlankToNull() {
        assertThat(Strings.trimToNull(" ")).isNull();
        assertThat(Strings.trimToNull(" value ")).isEqualTo("value");
    }

    @Test
    void shouldRequireNonBlankValue() {
        assertThat(Strings.required(" value ", "Value is required")).isEqualTo("value");

        assertThatThrownBy(() -> Strings.required(null, "Value is required"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value is required");
    }

    @Test
    void shouldNormalizeCodeByTrimmingAndUppercasingWithRootLocale() {
        assertThat(Strings.normalizeCode("  owner  ", "Code is required")).isEqualTo("OWNER");
        assertThat(Strings.normalizeCode("café", "Code is required")).isEqualTo("CAFÉ");
        assertThat(Strings.normalizeCode("order-item", "Code is required")).isEqualTo("ORDER-ITEM");
    }

    @Test
    void shouldRejectBlankCode() {
        assertThatThrownBy(() -> Strings.normalizeCode(null, "Code is required"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Code is required");

        assertThatThrownBy(() -> Strings.normalizeCode("   ", "Code is required"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Code is required");
    }
}
