package com.thiagsilvadev.erpapi.catalog.internal.repository;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.catalog.api.CategoryStatus;
import com.thiagsilvadev.erpapi.catalog.internal.dto.CategorySearchCriteria;
import com.thiagsilvadev.erpapi.catalog.internal.model.Category;

public class CategorySpecification {

    private CategorySpecification() {}

    public static Specification<Category> withCriteria(CategorySearchCriteria criteria) {
        return Specification.where(hasCode(criteria.code()))
                .and(hasStatus(criteria.status()))
                .and(hasNameLike(criteria.name()));
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
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }
}
