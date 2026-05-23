package com.thiagsilvadev.erpapi.common.domain.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class DomainAssertTest {

    private enum TestErrorCode implements DomainErrorCode {
        VALUE_MUST_BE_POSITIVE
    }

    @Test
    void shouldRejectNullValue() {
        Object value = System.getProperty("domain.assert.required.value");

        assertThatThrownBy(() -> DomainAssert.notNull(value, "Value is required"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Value is required");
    }

    @Test
    void shouldRejectBlankText() {
        String nullText = System.getProperty("domain.assert.blank.text");
        String blankText = " ".repeat(1);

        assertThatThrownBy(() -> DomainAssert.notBlank(nullText, "Value is required"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Value is required");
        assertThatThrownBy(() -> DomainAssert.notBlank(blankText, "Value is required"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Value is required");
    }

    @Test
    void shouldRejectTextAboveMaximumLength() {
        assertThatThrownBy(() -> DomainAssert.maxLength("abcd", 3, "Too long"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Too long");
    }

    @Test
    void shouldRejectTextWithDifferentLength() {
        assertThatThrownBy(() -> DomainAssert.exactLength("123", 4, "Invalid length"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid length");
    }

    @Test
    void shouldRejectTextOutsideLengthRange() {
        assertThatThrownBy(() -> DomainAssert.lengthBetween("1", 2, 3, "Invalid length"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid length");
        assertThatThrownBy(() -> DomainAssert.lengthBetween("1234", 2, 3, "Invalid length"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid length");
    }

    @Test
    void shouldRejectTextThatDoesNotMatchPattern() {
        Pattern pattern = Pattern.compile("\\d{3}");

        assertThatThrownBy(() -> DomainAssert.matchesPattern("abc", pattern, "Invalid format"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid format");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false})
    void shouldRejectWhenConditionIsFalse(boolean condition) {
        assertThatThrownBy(() -> DomainAssert.isTrue(condition, "Invalid condition"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid condition");
    }

    @Test
    void shouldRejectWithErrorCode() {
        assertThatThrownBy(() -> DomainAssert.requirePositive(0, TestErrorCode.VALUE_MUST_BE_POSITIVE))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage(TestErrorCode.VALUE_MUST_BE_POSITIVE.name())
                .extracting(ex -> ((DomainValidationException) ex).getErrorCode())
                .isEqualTo(TestErrorCode.VALUE_MUST_BE_POSITIVE);
    }

    @Test
    void shouldAllowValidValues() {
        Pattern pattern = Pattern.compile("\\d{3}");

        assertThatCode(() -> DomainAssert.notNull("value", "Value is required")).doesNotThrowAnyException();
        assertThatCode(() -> DomainAssert.notBlank("value", "Value is required"))
                .doesNotThrowAnyException();
        assertThatCode(() -> DomainAssert.maxLength("abc", 3, "Too long")).doesNotThrowAnyException();
        assertThatCode(() -> DomainAssert.exactLength("123", 3, "Invalid length"))
                .doesNotThrowAnyException();
        assertThatCode(() -> DomainAssert.lengthBetween("12", 2, 3, "Invalid length"))
                .doesNotThrowAnyException();
        assertThatCode(() -> DomainAssert.matchesPattern("123", pattern, "Invalid format"))
                .doesNotThrowAnyException();
        assertThatCode(() -> DomainAssert.isTrue(true, "Invalid condition")).doesNotThrowAnyException();
    }
}
