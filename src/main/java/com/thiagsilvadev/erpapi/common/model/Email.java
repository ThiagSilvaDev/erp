package com.thiagsilvadev.erpapi.common.model;

import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.validation.DomainAssert;

@Embeddable
public class Email {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    @Column(name = "email", length = 255, nullable = false)
    private String value;

    protected Email() {}

    private Email(String value) {
        this.value = processValue(value);
    }

    public static Email create(String value) {
        return new Email(value);
    }

    private static String processValue(String rawValue) {
        String normalizedValue = rawValue == null ? null : rawValue.trim().toLowerCase();

        DomainAssert.notBlank(normalizedValue, "Email is required");
        DomainAssert.maxLength(normalizedValue, 255, "Email must contain at most 255 characters");
        DomainAssert.matchesPattern(normalizedValue, EMAIL_PATTERN, "Invalid email format");
        return normalizedValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email email)) return false;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
