package com.thiagsilvadev.erpapi.customer.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerSearchCriteria;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;

public class CustomerSpecification {

    private CustomerSpecification() {}

    public static Specification<Customer> customers(CustomerSearchCriteria criteria) {
        return Specification.where(customerName(criteria.name()))
                .and(customerDocument(criteria.document()))
                .and(customerCompany(criteria.companyId()));
    }

    public static Specification<Supplier> suppliers(CustomerSearchCriteria criteria) {
        return Specification.where(supplierName(criteria.name()))
                .and(supplierDocument(criteria.document()))
                .and(supplierCompany(criteria.companyId()));
    }

    private static Specification<Customer> customerName(String name) {
        return (root, query, cb) -> like(name, root.get("name"), cb);
    }

    private static Specification<Supplier> supplierName(String name) {
        return (root, query, cb) -> like(name, root.get("name"), cb);
    }

    private static Specification<Customer> customerDocument(String document) {
        return (root, query, cb) -> document == null || document.isBlank()
                ? null
                : cb.equal(root.get("document"), Customer.normalizeDocument(document));
    }

    private static Specification<Supplier> supplierDocument(String document) {
        return (root, query, cb) -> document == null || document.isBlank()
                ? null
                : cb.equal(root.get("document"), Supplier.normalizeDocument(document));
    }

    private static Specification<Customer> customerCompany(UUID companyId) {
        return (root, query, cb) -> companyId == null ? null : cb.equal(root.get("companyId"), companyId);
    }

    private static Specification<Supplier> supplierCompany(UUID companyId) {
        return (root, query, cb) -> companyId == null ? null : cb.equal(root.get("companyId"), companyId);
    }

    private static jakarta.persistence.criteria.Predicate like(
            String value,
            jakarta.persistence.criteria.Path<String> path,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return cb.like(cb.lower(path), "%" + value.toLowerCase() + "%");
    }
}
