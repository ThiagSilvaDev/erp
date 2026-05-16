package com.thiagsilvadev.erpapi.catalog.internal.model;

import java.util.Objects;

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
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(
        name = "catalog_products",
        uniqueConstraints = {@UniqueConstraint(name = "uk_catalog_product_sku", columnNames = "sku")})
public class Product extends AbstractBaseEntity {

    @Embedded
    private Sku sku;

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
        this(Sku.create(sku), name, description, basePrice, category);
    }

    private Product(Sku sku, String name, String description, Money basePrice, Category category) {
        this.sku = Objects.requireNonNull(sku, "Product SKU cannot be null");
        this.name = Strings.required(name, "Product name cannot be blank");
        this.description = Strings.trimToNull(description);
        this.basePrice = Objects.requireNonNull(basePrice, "Product base price cannot be null");
        this.category = Objects.requireNonNull(category, "Product category cannot be null");
    }

    public static Product create(String sku, String name, String description, Money basePrice, Category category) {
        return new Product(sku, name, description, basePrice, category);
    }

    public static Product create(Sku sku, String name, String description, Money basePrice, Category category) {
        return new Product(sku, name, description, basePrice, category);
    }

    public void update(String name, String description, Money basePrice, Category category) {
        this.name = Strings.required(name, "Product name cannot be blank");
        this.description = Strings.trimToNull(description);
        this.basePrice = Objects.requireNonNull(basePrice, "Product base price cannot be null");
        this.category = Objects.requireNonNull(category, "Product category cannot be null");
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public String getSku() {
        return sku.getValue();
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
}
