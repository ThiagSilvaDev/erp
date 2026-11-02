package com.thiagsilvadev.erpapi.sales.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderSearchCriteria;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;

public class SalesOrderSpecification {

    private SalesOrderSpecification() {}

    public static Specification<SalesOrder> withCriteria(SalesOrderSearchCriteria criteria) {
        Specification<SalesOrder> saleType = (root, query, cb) ->
                criteria.saleType() == null ? null : cb.equal(root.get("saleType"), criteria.saleType());
        Specification<SalesOrder> seller = (root, query, cb) -> criteria.sellerCompanyId() == null
                ? null
                : cb.equal(root.get("sellerCompanyId"), criteria.sellerCompanyId());
        Specification<SalesOrder> buyer = (root, query, cb) -> criteria.buyerCompanyId() == null
                ? null
                : cb.equal(root.get("buyerCompanyId"), criteria.buyerCompanyId());
        Specification<SalesOrder> customer = (root, query, cb) ->
                criteria.customerId() == null ? null : cb.equal(root.get("customerId"), criteria.customerId());
        Specification<SalesOrder> status =
                (root, query, cb) -> criteria.status() == null ? null : cb.equal(root.get("status"), criteria.status());
        return Specification.allOf(saleType, seller, buyer, customer, status);
    }

    public static Specification<SalesOrder> withCriteriaForSeller(
            SalesOrderSearchCriteria criteria, UUID sellerCompanyId) {
        Specification<SalesOrder> tenantSeller =
                (root, query, cb) -> cb.equal(root.get("sellerCompanyId"), sellerCompanyId);
        return tenantSeller.and(withCriteria(criteria));
    }
}
