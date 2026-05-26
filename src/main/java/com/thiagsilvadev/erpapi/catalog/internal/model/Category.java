package com.thiagsilvadev.erpapi.catalog.internal.model;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;
import com.thiagsilvadev.erpapi.catalog.internal.exception.CategoryErrorCode;
import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

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
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static Category create(String code, String name, String description) {
        return new Category(
                normalizeCode(code),
                DomainAssert.requireNonBlank(name, CategoryErrorCode.NAME_REQUIRED),
                Strings.trimToNull(description));
    }

    public void update(String name, String description) {
        this.name = DomainAssert.requireNonBlank(name, CategoryErrorCode.NAME_REQUIRED);
        this.description = Strings.trimToNull(description);
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
        return DomainAssert.requireNonBlank(code, CategoryErrorCode.CODE_REQUIRED)
                .toUpperCase();
    }
}
