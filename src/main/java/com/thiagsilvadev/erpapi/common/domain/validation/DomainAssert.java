package com.thiagsilvadev.erpapi.common.domain.validation;

import java.util.regex.Pattern;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainErrorCode;
import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;

public final class DomainAssert {

    private DomainAssert() {}

    @Deprecated(since = "0.0.1")
    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new DomainValidationException(message);
        }
    }

    @Deprecated(since = "0.0.1")
    public static void notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
    }

    @Deprecated(since = "0.0.1")
    public static void maxLength(String value, int maxLength, String message) {
        if (value != null && value.length() > maxLength) {
            throw new DomainValidationException(message);
        }
    }

    @Deprecated(since = "0.0.1")
    public static void exactLength(String value, int length, String message) {
        if (value == null || value.length() != length) {
            throw new DomainValidationException(message);
        }
    }

    @Deprecated(since = "0.0.1")
    public static void lengthBetween(String value, int minLength, int maxLength, String message) {
        if (value == null || value.length() < minLength || value.length() > maxLength) {
            throw new DomainValidationException(message);
        }
    }

    @Deprecated(since = "0.0.1")
    public static void matchesPattern(String value, Pattern pattern, String message) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw new DomainValidationException(message);
        }
    }

    @Deprecated(since = "0.0.1")
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new DomainValidationException(message);
        }
    }

    public static int requireNonNegative(int value, DomainErrorCode errorCode) {
        if (value < 0) {
            throw new DomainValidationException(errorCode);
        }
        return value;
    }

    public static <T> T requireNonNull(T value, DomainErrorCode errorCode) {
        if (value == null) {
            throw new DomainValidationException(errorCode);
        }
        return value;
    }

    public static String requireNonBlank(String value, DomainErrorCode errorCode) {
        requireNonNull(value, errorCode);
        if (value.isBlank()) {
            throw new DomainValidationException(errorCode);
        }
        return value.trim();
    }

    public static int requirePositive(int value, DomainErrorCode errorCode) {
        if (value <= 0) {
            throw new DomainValidationException(errorCode);
        }
        return value;
    }

    public static <T> T requireNotNull(T value, String attribute) {
        if (value == null) {
            throw InvalidAttributeException.cannotBeNull(attribute);
        }
        return value;
    }
}
