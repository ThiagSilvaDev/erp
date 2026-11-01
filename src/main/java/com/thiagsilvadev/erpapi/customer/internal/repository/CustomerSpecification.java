package com.thiagsilvadev.erpapi.customer.internal.repository;

import java.util.UUID;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerSearchCriteria;
import com.thiagsilvadev.erpapi.customer.internal.dto.SupplierSearchCriteria;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;

public class CustomerSpecification {

    private CustomerSpecification() {}

    public static Specification<Customer> customers(CustomerSearchCriteria criteria) {
        return Specification.where(customerName(criteria.name()))
                .and(customerDocument(criteria.document()))
                .and(customerOwnerCompany(criteria.ownerCompanyId()));
    }

    public static Specification<Customer> customersForCompany(CustomerSearchCriteria criteria, UUID ownerCompanyId) {
        return Specification.where(customerOwnerCompany(ownerCompanyId)).and(customers(criteria));
    }

    public static Specification<Supplier> suppliers(SupplierSearchCriteria criteria) {
        return Specification.where(supplierName(criteria.name()))
                .and(supplierDocument(criteria.document()))
                .and(supplierCompany(criteria.companyId()));
    }

    public static Specification<Supplier> suppliersForCompany(SupplierSearchCriteria criteria, UUID companyId) {
        return Specification.where(supplierCompany(companyId)).and(suppliers(criteria));
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

    private static Specification<Customer> customerOwnerCompany(UUID ownerCompanyId) {
        return (root, query, cb) ->
                ownerCompanyId == null ? null : cb.equal(root.get("ownerCompanyId"), ownerCompanyId);
    }

    private static Specification<Supplier> supplierCompany(UUID companyId) {
        return (root, query, cb) -> companyId == null ? null : cb.equal(root.get("companyId"), companyId);
    }

    private static Predicate like(String value, Path<String> path, CriteriaBuilder cb) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return cb.like(cb.lower(path), "%" + value.toLowerCase() + "%");
    }
}
