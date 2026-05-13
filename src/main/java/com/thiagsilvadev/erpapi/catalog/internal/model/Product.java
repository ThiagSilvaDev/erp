package com.thiagsilvadev.erpapi.catalog.internal.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;
import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;

@Entity
@Table(
        name = "catalog_products",
        uniqueConstraints = {@UniqueConstraint(name = "uk_catalog_product_sku", columnNames = "sku")})
public class Product extends AbstractBaseEntity {

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected Product() {}

    private Product(String sku, String name, String description, BigDecimal basePrice, Category category) {
        this.sku = normalizeSku(sku);
        this.name = normalizeRequired(name, "Product name cannot be blank");
        this.description = trimToNull(description);
        this.basePrice = normalizeBasePrice(basePrice);
        this.category = requireCategory(category);
    }

    public static Product create(String sku, String name, String description, BigDecimal basePrice, Category category) {
        return new Product(sku, name, description, basePrice, category);
    }

    public void update(String name, String description, BigDecimal basePrice, Category category) {
        this.name = normalizeRequired(name, "Product name cannot be blank");
        this.description = trimToNull(description);
        this.basePrice = normalizeBasePrice(basePrice);
        this.category = requireCategory(category);
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }

    public static String normalizeSku(String sku) {
        return normalizeRequired(sku, "Product SKU cannot be blank").toUpperCase();
    }

    private static BigDecimal normalizeBasePrice(BigDecimal basePrice) {
        if (basePrice == null) {
            throw new IllegalArgumentException("Product base price cannot be null");
        }
        if (basePrice.signum() < 0) {
            throw new IllegalArgumentException("Product base price cannot be negative");
        }
        return basePrice;
    }

    private static Category requireCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Product category cannot be null");
        }
        return category;
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
