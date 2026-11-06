package com.thiagsilvadev.erpapi.common.domain.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;

class StringsTest {

    @Test
    void shouldTrimBlankToNull() {
        assertThat(Strings.trimToNull(" ")).isNull();
        assertThat(Strings.trimToNull(" value ")).isEqualTo("value");
    }

    @Test
    void shouldRequireNonBlankValue() {
        assertThat(Strings.required(" value ", "Value")).isEqualTo("value");

        assertThatThrownBy(() -> Strings.required(null, "Value"))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Value cannot be blank");
    }

    @Test
    void shouldNormalizeCodeByTrimmingAndUppercasingWithRootLocale() {
        assertThat(Strings.normalizeCode("  owner  ", "Code")).isEqualTo("OWNER");
        assertThat(Strings.normalizeCode("café", "Code")).isEqualTo("CAFÉ");
        assertThat(Strings.normalizeCode("order-item", "Code")).isEqualTo("ORDER-ITEM");
    }

    @Test
    void shouldRejectBlankCode() {
        assertThatThrownBy(() -> Strings.normalizeCode(null, "Code"))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Code cannot be blank");

        assertThatThrownBy(() -> Strings.normalizeCode("   ", "Code"))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Code cannot be blank");
    }
}
