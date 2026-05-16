package com.thiagsilvadev.erpapi.catalog.internal.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Embeddable
public class Sku {

    @Column(name = "sku", nullable = false, length = 80)
    private String value;

    protected Sku() {}

    private Sku(String value) {
        this.value = normalize(value);
    }

    public static Sku create(String value) {
        return new Sku(value);
    }

    private static String normalize(String value) {
        return Strings.required(value, "Product SKU cannot be blank").toUpperCase();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sku sku)) return false;
        return Objects.equals(value, sku.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
