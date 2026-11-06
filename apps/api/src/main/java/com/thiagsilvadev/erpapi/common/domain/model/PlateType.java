package com.thiagsilvadev.erpapi.common.domain.model;

import java.util.regex.Pattern;

public enum PlateType {
    MERCOSUL(
            Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$"),
            Pattern.compile("^[A-Z]{3}[-\\s]?\\d[A-Z]\\d{2}$", Pattern.CASE_INSENSITIVE),
            "Mercosul plate (AAA 1A23)"),

    BRAZIL_LEGACY(
            Pattern.compile("^[A-Z]{3}\\d{4}$"),
            Pattern.compile("^[A-Z]{3}[-\\s]?\\d{4}$", Pattern.CASE_INSENSITIVE),
            "Brazil legacy plate (AAA 1234)"),

    US(
            Pattern.compile("^[A-Z0-9]{1,7}$"),
            Pattern.compile("^[A-Z0-9\\s-]{1,8}$", Pattern.CASE_INSENSITIVE),
            "US plate (up to 7 alphanumeric characters)"),

    GENERIC(
            Pattern.compile("^[A-Z0-9]{1,10}$"),
            Pattern.compile("^[A-Z0-9\\s-]{1,12}$", Pattern.CASE_INSENSITIVE),
            "Generic plate (up to 10 alphanumeric characters)");

    private final Pattern normalizedPattern;
    private final Pattern rawPattern;
    private final String description;

    PlateType(Pattern normalizedPattern, Pattern rawPattern, String description) {
        this.normalizedPattern = normalizedPattern;
        this.rawPattern = rawPattern;
        this.description = description;
    }

    public Pattern getNormalizedPattern() {
        return normalizedPattern;
    }

    public Pattern getRawPattern() {
        return rawPattern;
    }

    public String getDescription() {
        return description;
    }
}
