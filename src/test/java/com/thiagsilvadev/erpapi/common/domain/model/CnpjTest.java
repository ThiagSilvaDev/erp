package com.thiagsilvadev.erpapi.common.domain.model;

import static com.thiagsilvadev.erpapi.support.ValueObjectAssertions.assertValueEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class CnpjTest {

    @Test
    void shouldNormalizeCnpjWithMask() {
        Cnpj cnpj = Cnpj.create(" 11.222.333/0001-81 ");

        assertThat(cnpj.getNumber()).isEqualTo("11222333000181");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectCnpjRequiredInvalid(String invalidCnpj) {
        assertThatThrownBy(() -> Cnpj.create(invalidCnpj))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("CNPJ is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1122233300018", "112223330001810"})
    void shouldRejectCnpjWithInvalidDigitQuantity(String invalidLengthCnpj) {
        assertThatThrownBy(() -> Cnpj.create(invalidLengthCnpj))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("CNPJ must contain 14 digits");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "00000000000000", "11111111111111", "22222222222222", "33333333333333",
                "44444444444444", "55555555555555", "66666666666666", "77777777777777",
                "88888888888888", "99999999999999"
            })
    void shouldRejectCnpjWithDigitsRepeated(String repeatedCnpj) {
        assertThatThrownBy(() -> Cnpj.create(repeatedCnpj))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid CNPJ");
    }

    @Test
    void shouldRejectCnpjWithDigitCheckInvalid() {
        assertThatThrownBy(() -> Cnpj.create("11.222.333/0001-80"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid CNPJ");
    }

    @Test
    void shouldCompareCnpjsByValue() {
        Cnpj cnpj = Cnpj.create("11.222.333/0001-81");
        Cnpj equalCnpj = Cnpj.create("11222333000181");
        Cnpj differentCnpj = Cnpj.create("04.252.011/0001-10");

        assertValueEquality(cnpj, equalCnpj, differentCnpj);
    }
}
