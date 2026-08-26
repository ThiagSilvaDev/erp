package com.thiagsilvadev.erpapi.common.domain.validation;

import java.util.Locale;

import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;

public final class Strings {

    private Strings() {}

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String required(String value, String attribute) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw InvalidAttributeException.cannotBeBlank(attribute);
        }
        return normalized;
    }

    public static String normalizeCode(String value, String attribute) {
        return required(value, attribute).toUpperCase(Locale.ROOT);
    }
}
