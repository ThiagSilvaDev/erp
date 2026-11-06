package com.thiagsilvadev.erpapi.catalog.internal.repository;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.catalog.api.ProductStatus;
import com.thiagsilvadev.erpapi.catalog.internal.dto.product.ProductSearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> withCriteria(ProductSearchCriteria criteria) {
        return Specification.allOf(
                hasSku(criteria.sku()),
                hasStatus(criteria.status()),
                hasCategory(criteria.categoryId()),
                hasNameLike(criteria.name()));
    }

    public static Specification<Product> withCriteriaForCompany(ProductSearchCriteria criteria, UUID companyId) {
        return Specification.allOf(hasCompany(companyId), withCriteria(criteria));
    }

    private static Specification<Product> hasCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    private static Specification<Product> hasSku(String sku) {
        return (root, query, cb) -> {
            if (sku == null || sku.isBlank()) {
                return null;
            }
            return cb.equal(root.get("sku").get("value"), Sku.of(sku).getValue());
        };
    }

    private static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<Product> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("name")), "%" + escapeLike(name.trim().toLowerCase(Locale.ROOT)) + "%", '\\');
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
