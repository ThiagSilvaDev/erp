package com.thiagsilvadev.erpapi.catalog.internal.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;
import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Money;

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

    @Embedded
    @AttributeOverride(
            name = "amount",
            column = @Column(name = "base_price_amount", nullable = false, precision = 19, scale = 2))
    @AttributeOverride(
            name = "currencyCode",
            column = @Column(name = "base_price_currency_code", nullable = false, length = 3))
    private Money basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected Product() {}

    private Product(String sku, String name, String description, Money basePrice, Category category) {
        this.sku = normalizeSku(sku);
        this.name = normalizeRequired(name, "Product name cannot be blank");
        this.description = trimToNull(description);
        this.basePrice = requireBasePrice(basePrice);
        this.category = requireCategory(category);
    }

    public static Product create(String sku, String name, String description, Money basePrice, Category category) {
        return new Product(sku, name, description, basePrice, category);
    }

    public void update(String name, String description, Money basePrice, Category category) {
        this.name = normalizeRequired(name, "Product name cannot be blank");
        this.description = trimToNull(description);
        this.basePrice = requireBasePrice(basePrice);
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

    public Money getBasePrice() {
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

    private static Money requireBasePrice(Money basePrice) {
        if (basePrice == null) {
            throw new IllegalArgumentException("Product base price cannot be null");
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
