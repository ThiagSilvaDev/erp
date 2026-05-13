package com.thiagsilvadev.erpapi.catalog.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;
import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;

@Entity
@Table(
        name = "catalog_categories",
        uniqueConstraints = {@UniqueConstraint(name = "uk_catalog_category_code", columnNames = "code")})
public class Category extends AbstractBaseEntity {

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status = CategoryStatus.ACTIVE;

    protected Category() {}

    private Category(String code, String name, String description) {
        this.code = normalizeCode(code);
        this.name = normalizeRequired(name, "Category name cannot be blank");
        this.description = trimToNull(description);
    }

    public static Category create(String code, String name, String description) {
        return new Category(code, name, description);
    }

    public void update(String name, String description) {
        this.name = normalizeRequired(name, "Category name cannot be blank");
        this.description = trimToNull(description);
    }

    public void activate() {
        this.status = CategoryStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = CategoryStatus.INACTIVE;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CategoryStatus getStatus() {
        return status;
    }

    public static String normalizeCode(String code) {
        return normalizeRequired(code, "Category code cannot be blank").toUpperCase();
    }

    private static String normalizeRequired(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
