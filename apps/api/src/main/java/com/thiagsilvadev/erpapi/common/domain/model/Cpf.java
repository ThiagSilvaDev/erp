package com.thiagsilvadev.erpapi.common.domain.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

@Embeddable
public class Cpf {

    @Column(name = "cpf", length = 11, nullable = false)
    private String number;

    protected Cpf() {}

    private Cpf(String number) {
        this.number = processNumber(number);
    }

    public static Cpf create(String number) {
        return new Cpf(number);
    }

    private static String processNumber(String rawNumber) {
        DomainAssert.notBlank(rawNumber, "CPF is required");

        String normalizedNumber = rawNumber.replaceAll("\\D", "");

        DomainAssert.exactLength(normalizedNumber, 11, "CPF must contain 11 digits");
        DomainAssert.isTrue(
                !hasAllDigitsEqual(normalizedNumber) && hasValidCheckDigits(normalizedNumber), "Invalid CPF");
        return normalizedNumber;
    }

    private static boolean hasAllDigitsEqual(String number) {
        char firstDigit = number.charAt(0);

        for (int index = 1; index < number.length(); index++) {
            if (number.charAt(index) != firstDigit) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasValidCheckDigits(String number) {
        int firstDigit = calculateDigit(number, 9, 10);
        int secondDigit = calculateDigit(number, 10, 11);

        return firstDigit == Character.digit(number.charAt(9), 10)
                && secondDigit == Character.digit(number.charAt(10), 10);
    }

    private static int calculateDigit(String number, int digitCount, int initialWeight) {
        int sum = 0;

        for (int index = 0; index < digitCount; index++) {
            sum += Character.digit(number.charAt(index), 10) * (initialWeight - index);
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cpf cpf)) return false;
        return Objects.equals(number, cpf.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
