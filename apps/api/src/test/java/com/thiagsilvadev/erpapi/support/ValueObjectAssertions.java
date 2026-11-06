package com.thiagsilvadev.erpapi.support;

import static org.assertj.core.api.Assertions.assertThat;

public final class ValueObjectAssertions {

    private ValueObjectAssertions() {}

    public static void assertValueEquality(Object actual, Object equalValue, Object differentValue) {
        assertThat(actual).isEqualTo(equalValue);
        assertThat(actual).hasSameHashCodeAs(equalValue);
        assertThat(actual).isNotEqualTo(differentValue);
    }
}
