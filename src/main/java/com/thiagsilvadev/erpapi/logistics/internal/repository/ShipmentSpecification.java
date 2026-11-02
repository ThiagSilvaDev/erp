package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentSearchCriteria;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;

public class ShipmentSpecification {

    private ShipmentSpecification() {}

    public static Specification<Shipment> withCriteria(ShipmentSearchCriteria criteria) {
        Specification<Shipment> salesOrder = (root, query, cb) ->
                criteria.salesOrderId() == null ? null : cb.equal(root.get("salesOrderId"), criteria.salesOrderId());
        Specification<Shipment> driver = (root, query, cb) ->
                criteria.driverId() == null ? null : cb.equal(root.get("driver").get("id"), criteria.driverId());
        Specification<Shipment> vehicle = (root, query, cb) -> criteria.vehicleId() == null
                ? null
                : cb.equal(root.get("vehicle").get("id"), criteria.vehicleId());
        Specification<Shipment> status =
                (root, query, cb) -> criteria.status() == null ? null : cb.equal(root.get("status"), criteria.status());
        return Specification.allOf(salesOrder, driver, vehicle, status);
    }

    public static Specification<Shipment> withCriteriaForSeller(ShipmentSearchCriteria criteria, UUID sellerCompanyId) {
        Specification<Shipment> sellerScope =
                (root, query, cb) -> cb.equal(root.get("sellerCompanyId"), sellerCompanyId);
        return Specification.allOf(sellerScope, withCriteria(criteria));
    }
}
