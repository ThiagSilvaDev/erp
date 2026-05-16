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
}
