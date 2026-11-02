package com.thiagsilvadev.erpapi.catalog.internal.repository;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;
import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;

public class CategorySpecification {

    private CategorySpecification() {}

    public static Specification<Category> withCriteria(CategorySearchCriteria criteria) {
        return Specification.allOf(
                hasCode(criteria.code()), hasStatus(criteria.status()), hasNameLike(criteria.name()));
    }

    public static Specification<Category> withCriteriaForCompany(CategorySearchCriteria criteria, UUID companyId) {
        return Specification.allOf(hasCompany(companyId), withCriteria(criteria));
    }

    private static Specification<Category> hasCompany(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    private static Specification<Category> hasCode(String code) {
        return (root, query, cb) -> {
            if (code == null || code.isBlank()) {
                return null;
            }
            return cb.equal(root.get("code"), Category.normalizeCode(code));
        };
    }

    private static Specification<Category> hasStatus(CategoryStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Category> hasNameLike(String name) {
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
