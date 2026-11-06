package com.thiagsilvadev.erpapi.common.domain.model;

import static com.thiagsilvadev.erpapi.support.ValueObjectAssertions.assertValueEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class PhoneTest {

    @Test
    void shouldNormalizeRequiredPhone() {
        Phone phone = Phone.of(" (11) 91234-5678 ");

        assertThat(phone.getValue()).isEqualTo("11912345678");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankRequiredPhone(String blankPhone) {
        assertThatThrownBy(() -> Phone.of(blankPhone))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Phone cannot be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldReturnNullForOptionalBlankPhone(String blankPhone) {
        assertThat(Phone.optional(blankPhone)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "123456789012"})
    void shouldRejectPhoneWithInvalidDigitQuantity(String invalidLengthPhone) {
        assertThatThrownBy(() -> Phone.of(invalidLengthPhone))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Phone with area code must contain 10 or 11 digits");
    }

    @Test
    void shouldComparePhonesByValue() {
        Phone phone = Phone.of("11912345678");
        Phone equalPhone = Phone.of("(11) 91234-5678");
        Phone differentPhone = Phone.of("1112345678");

        assertValueEquality(phone, equalPhone, differentPhone);
    }
}
