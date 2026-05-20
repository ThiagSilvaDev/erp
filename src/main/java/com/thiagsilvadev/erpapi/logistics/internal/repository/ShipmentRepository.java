package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID>, JpaSpecificationExecutor<Shipment> {

    boolean existsBySalesOrderId(UUID salesOrderId);

    Optional<Shipment> findBySalesOrderId(UUID salesOrderId);
}
