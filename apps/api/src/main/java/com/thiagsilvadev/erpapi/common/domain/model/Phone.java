package com.thiagsilvadev.erpapi.common.domain.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

@Embeddable
public class Phone {

    @Column(name = "phone", length = 11)
    private String value;

    protected Phone() {}

    private Phone(String value) {
        this.value = value;
    }

    public static Phone of(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            throw new DomainValidationException("Phone cannot be blank");
        }
        return new Phone(normalizedValue);
    }

    public static Phone optional(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue == null ? null : new Phone(normalizedValue);
    }

    private static String normalize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalizedValue = rawValue.replaceAll("\\D", "");
        DomainAssert.lengthBetween(normalizedValue, 10, 11, "Phone with area code must contain 10 or 11 digits");
        return normalizedValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Phone phone)) return false;
        return Objects.equals(value, phone.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
