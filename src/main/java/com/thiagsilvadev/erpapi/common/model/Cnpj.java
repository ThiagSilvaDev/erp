package com.thiagsilvadev.erpapi.common.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.validation.DomainAssert;

@Embeddable
public class Cnpj {

    @Column(name = "cnpj", length = 14, nullable = false)
    private String number;

    protected Cnpj() {}

    private Cnpj(String number) {
        this.number = processNumber(number);
    }

    public static Cnpj create(String number) {
        return new Cnpj(number);
    }

    private static String processNumber(String rawNumber) {
        DomainAssert.notBlank(rawNumber, "CNPJ is required");

        String normalizedNumber = rawNumber.replaceAll("\\D", "");

        DomainAssert.exactLength(normalizedNumber, 14, "CNPJ must contain 14 digits");
        DomainAssert.isTrue(
                !hasAllDigitsEqual(normalizedNumber) && hasValidCheckDigits(normalizedNumber), "Invalid CNPJ");
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
        int firstDigit = calculateDigit(number, new int[] {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        int secondDigit = calculateDigit(number, new int[] {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});

        return firstDigit == Character.digit(number.charAt(12), 10)
                && secondDigit == Character.digit(number.charAt(13), 10);
    }

    private static int calculateDigit(String number, int[] weights) {
        int sum = 0;

        for (int index = 0; index < weights.length; index++) {
            sum += Character.digit(number.charAt(index), 10) * weights[index];
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
        if (!(o instanceof Cnpj cnpj)) return false;
        return Objects.equals(number, cnpj.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
