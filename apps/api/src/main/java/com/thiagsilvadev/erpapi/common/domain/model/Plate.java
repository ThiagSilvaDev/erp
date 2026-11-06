package com.thiagsilvadev.erpapi.common.domain.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

@Embeddable
public class Plate {

    @Column(name = "plate", nullable = false, length = 10)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "plate_type", nullable = false, length = 20)
    private PlateType type;

    protected Plate() {}

    private Plate(String value, PlateType type) {
        this.value = value;
        this.type = type;
    }

    public static Plate of(String rawValue, PlateType type) {
        DomainAssert.notNull(type, "Plate type is required");
        String sanitized = sanitize(rawValue);
        DomainAssert.notBlank(sanitized, "Plate is required");
        DomainAssert.matchesPattern(
                sanitized,
                type.getNormalizedPattern(),
                String.format("Invalid %s format", type.getDescription().toLowerCase()));
        return new Plate(sanitized, type);
    }

    public static String sanitize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return rawValue.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public String getValue() {
        return value;
    }

    public PlateType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plate plate)) return false;
        return Objects.equals(value, plate.value) && type == plate.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }
}
