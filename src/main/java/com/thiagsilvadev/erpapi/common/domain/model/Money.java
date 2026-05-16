package com.thiagsilvadev.erpapi.common.domain.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

@Embeddable
public class Money {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    protected Money() {}

    private Money(BigDecimal amount, String currencyCode) {
        this.amount = processAmount(amount);
        this.currencyCode = processCurrencyCode(currencyCode);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }

    public static Money reais(BigDecimal amount) {
        return new Money(amount, "BRL");
    }

    private static BigDecimal processAmount(BigDecimal amount) {
        DomainAssert.notNull(amount, "Money amount is required");
        DomainAssert.isTrue(amount.signum() >= 0, "Money amount cannot be negative");
        DomainAssert.isTrue(amount.scale() <= 2, "Money amount must contain at most 2 decimal places");
        return amount;
    }

    private static String processCurrencyCode(String currencyCode) {
        DomainAssert.notBlank(currencyCode, "Currency code is required");

        String normalizedCurrencyCode = currencyCode.trim().toUpperCase();
        try {
            Currency.getInstance(normalizedCurrencyCode);
        } catch (IllegalArgumentException exception) {
            throw new DomainValidationException("Invalid currency code");
        }
        return normalizedCurrencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return Objects.equals(amount, money.amount) && Objects.equals(currencyCode, money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }
}
