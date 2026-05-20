package com.thiagsilvadev.erpapi.logistics.internal.repository;

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
        return salesOrder.and(driver).and(vehicle).and(status);
    }
}
