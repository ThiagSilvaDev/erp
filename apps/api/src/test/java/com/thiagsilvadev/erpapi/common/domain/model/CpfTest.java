package com.thiagsilvadev.erpapi.common.domain.model;

import static com.thiagsilvadev.erpapi.support.ValueObjectAssertions.assertValueEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class CpfTest {

    @Test
    void shouldNormalizeCpfWithMask() {
        Cpf cpf = Cpf.create(" 529.982.247-25 ");

        assertThat(cpf.getNumber()).isEqualTo("52998224725");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectCpfRequiredInvalid(String invalidCpf) {
        assertThatThrownBy(() -> Cpf.create(invalidCpf))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("CPF is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"5299822472", "529982247250"})
    void shouldRejectCpfWithInvalidDigitQuantity(String invalidCpf) {
        assertThatThrownBy(() -> Cpf.create(invalidCpf))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("CPF must contain 11 digits");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "00000000000", "11111111111", "22222222222", "33333333333", "44444444444",
                "55555555555", "66666666666", "77777777777", "88888888888", "99999999999"
            })
    void shouldRejectCpfWithDigitsRepeated(String repeatedCpf) {
        assertThatThrownBy(() -> Cpf.create(repeatedCpf))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid CPF");
    }

    @Test
    void shouldRejectCpfWithDigitCheckInvalid() {
        assertThatThrownBy(() -> Cpf.create("529.982.247-24"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid CPF");
    }

    @Test
    void shouldCompareCpfsByValue() {
        Cpf cpf = Cpf.create("529.982.247-25");
        Cpf equalCpf = Cpf.create("52998224725");
        Cpf differentCpf = Cpf.create("168.995.350-09");

        assertValueEquality(cpf, equalCpf, differentCpf);
    }
}
