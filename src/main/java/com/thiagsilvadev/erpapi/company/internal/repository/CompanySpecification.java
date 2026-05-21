package com.thiagsilvadev.erpapi.company.internal.repository;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanySearchCriteria;
import com.thiagsilvadev.erpapi.company.internal.model.Company;

public class CompanySpecification {

    private CompanySpecification() {}

    public static Specification<Company> withCriteria(CompanySearchCriteria criteria) {
        return Specification.where(hasType(criteria.type()))
                .and(hasStatus(criteria.status()))
                .and(hasCnpj(criteria.cnpj()))
                .and(hasNameLike(criteria.name()));
    }

    private static Specification<Company> hasType(CompanyType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    private static Specification<Company> hasStatus(CompanyStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Company> hasCnpj(String cnpjStr) {
        return (root, query, cb) -> {
            if (cnpjStr == null || cnpjStr.isBlank()) {
                return null;
            }

            String digits = cnpjStr.replaceAll("\\D", "");

            if (digits.isBlank()) {
                return null;
            }

            return cb.like(root.get("cnpj").get("number"), digits + "%");
        };
    }

    private static Specification<Company> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("legalName")), pattern),
                    cb.like(cb.lower(root.get("tradeName")), pattern));
        };
    }
}
