package com.thiagsilvadev.erpapi.common.validation;

import java.util.regex.Pattern;

import com.thiagsilvadev.erpapi.common.exception.DomainValidationException;

public final class DomainAssert {

    private DomainAssert() {}

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new DomainValidationException(message);
        }
    }

    public static void notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
    }

    public static void maxLength(String value, int maxLength, String message) {
        if (value != null && value.length() > maxLength) {
            throw new DomainValidationException(message);
        }
    }

    public static void exactLength(String value, int length, String message) {
        if (value == null || value.length() != length) {
            throw new DomainValidationException(message);
        }
    }

    public static void lengthBetween(String value, int minLength, int maxLength, String message) {
        if (value == null || value.length() < minLength || value.length() > maxLength) {
            throw new DomainValidationException(message);
        }
    }

    public static void matchesPattern(String value, Pattern pattern, String message) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw new DomainValidationException(message);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new DomainValidationException(message);
        }
    }
}
