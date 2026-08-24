package com.thiagsilvadev.erpapi.common.domain.validation;

import java.util.Locale;

public final class Strings {

    private Strings() {}

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String required(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    public static String normalizeCode(String value, String message) {
        return required(value, message).toUpperCase(Locale.ROOT);
    }
}
