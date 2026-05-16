package com.thiagsilvadev.erpapi.common.domain.model;

import static com.thiagsilvadev.erpapi.support.ValueObjectAssertions.assertValueEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;

class MoneyTest {

    @Test
    void shouldNormalizeCurrencyCode() {
        Money money = Money.of(new BigDecimal("10.50"), " brl ");

        assertThat(money.getAmount()).isEqualByComparingTo("10.50");
        assertThat(money.getCurrencyCode()).isEqualTo("BRL");
    }

    @Test
    void shouldCreateReais() {
        Money money = Money.reais(new BigDecimal("10.50"));

        assertThat(money.getAmount()).isEqualByComparingTo("10.50");
        assertThat(money.getCurrencyCode()).isEqualTo("BRL");
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> Money.of(null, "BRL"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Money amount is required");
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-0.01"), "BRL"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Money amount cannot be negative");
    }

    @Test
    void shouldRejectAmountWithMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("10.999"), "BRL"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Money amount must contain at most 2 decimal places");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectRequiredInvalidCurrencyCode(String invalidCurrencyCode) {
        assertThatThrownBy(() -> Money.of(BigDecimal.TEN, invalidCurrencyCode))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Currency code is required");
    }

    @Test
    void shouldRejectInvalidCurrencyCode() {
        assertThatThrownBy(() -> Money.of(BigDecimal.TEN, "XYZ"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Invalid currency code");
    }

    @Test
    void shouldCompareMoneyByValue() {
        Money money = Money.of(new BigDecimal("10.50"), "BRL");
        Money equalMoney = Money.of(new BigDecimal("10.50"), "brl");
        Money differentMoney = Money.of(new BigDecimal("10.51"), "BRL");

        assertValueEquality(money, equalMoney, differentMoney);
    }
}
