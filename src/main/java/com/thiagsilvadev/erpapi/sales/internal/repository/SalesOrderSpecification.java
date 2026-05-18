package com.thiagsilvadev.erpapi.sales.internal.repository;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderSearchCriteria;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;

public class SalesOrderSpecification {

    private SalesOrderSpecification() {}

    public static Specification<SalesOrder> withCriteria(SalesOrderSearchCriteria criteria) {
        Specification<SalesOrder> customer = (root, query, cb) ->
                criteria.customerId() == null ? null : cb.equal(root.get("customerId"), criteria.customerId());
        Specification<SalesOrder> status =
                (root, query, cb) -> criteria.status() == null ? null : cb.equal(root.get("status"), criteria.status());
        return customer.and(status);
    }
}
