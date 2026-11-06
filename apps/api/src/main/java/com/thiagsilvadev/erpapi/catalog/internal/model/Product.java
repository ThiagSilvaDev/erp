package com.thiagsilvadev.erpapi.catalog.internal.model;

import java.util.UUID;

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
import com.thiagsilvadev.erpapi.catalog.internal.exception.ProductErrorCode;
import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(
        name = "catalog_products",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_catalog_product_company_sku",
                    columnNames = {"company_id", "sku"})
        })
public class Product extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

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

    private Product(UUID companyId, Sku sku, String name, String description, Money basePrice, Category category) {
        this.companyId = DomainAssert.requireNonNull(companyId, ProductErrorCode.CATEGORY_REQUIRED);
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.category = category;
    }

    public static Product create(
            UUID companyId, String sku, String name, String description, Money basePrice, Category category) {
        return create(companyId, Sku.of(sku), name, description, basePrice, category);
    }

    public static Product create(
            UUID companyId, Sku sku, String name, String description, Money basePrice, Category category) {
        return new Product(
                companyId,
                DomainAssert.requireNonNull(sku, ProductErrorCode.SKU_REQUIRED),
                DomainAssert.requireNonBlank(name, ProductErrorCode.NAME_REQUIRED),
                Strings.trimToNull(description),
                DomainAssert.requireNonNull(basePrice, ProductErrorCode.BASE_PRICE_REQUIRED),
                DomainAssert.requireNonNull(category, ProductErrorCode.CATEGORY_REQUIRED));
    }

    public void update(String name, String description, Money basePrice, Category category) {
        this.name = DomainAssert.requireNonBlank(name, ProductErrorCode.NAME_REQUIRED);
        this.description = Strings.trimToNull(description);
        this.basePrice = DomainAssert.requireNonNull(basePrice, ProductErrorCode.BASE_PRICE_REQUIRED);
        this.category = DomainAssert.requireNonNull(category, ProductErrorCode.CATEGORY_REQUIRED);
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

    public UUID getCompanyId() {
        return companyId;
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
