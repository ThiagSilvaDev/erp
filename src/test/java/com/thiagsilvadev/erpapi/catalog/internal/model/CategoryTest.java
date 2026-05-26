package com.thiagsilvadev.erpapi.catalog.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.catalog.internal.exception.CategoryErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class CategoryTest {

    @Test
    void shouldNormalizeCodeAndName() {
        Category category = Category.create(" raw ", " Raw materials ", "  ");

        assertThat(category.getCode()).isEqualTo("RAW");
        assertThat(category.getName()).isEqualTo("Raw materials");
        assertThat(category.getDescription()).isNull();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void shouldRejectRequiredCode(String code) {
        String name = "Raw materials";

        var exception = catchThrowableOfType(DomainValidationException.class, () -> Category.create(code, name, null));

        assertThat(exception.getErrorCode()).isEqualTo(CategoryErrorCode.CODE_REQUIRED);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void shouldRejectRequiredNameOnCreate(String name) {
        String code = "RAW";

        var exception = catchThrowableOfType(DomainValidationException.class, () -> Category.create(code, name, null));

        assertThat(exception.getErrorCode()).isEqualTo(CategoryErrorCode.NAME_REQUIRED);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void shouldRejectRequiredNameOnUpdate(String name) {
        Category category = Category.create("RAW", "Raw materials", null);

        var exception = catchThrowableOfType(DomainValidationException.class, () -> category.update(name, null));

        assertThat(exception.getErrorCode()).isEqualTo(CategoryErrorCode.NAME_REQUIRED);
    }
}
