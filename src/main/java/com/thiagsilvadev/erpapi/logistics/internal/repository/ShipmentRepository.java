package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID>, JpaSpecificationExecutor<Shipment> {

    boolean existsBySalesOrderId(UUID salesOrderId);

    boolean existsBySellerCompanyIdAndSalesOrderId(UUID sellerCompanyId, UUID salesOrderId);

    Optional<Shipment> findBySalesOrderId(UUID salesOrderId);

    Optional<Shipment> findBySellerCompanyIdAndSalesOrderId(UUID sellerCompanyId, UUID salesOrderId);

    Optional<Shipment> findByIdAndSellerCompanyId(UUID id, UUID sellerCompanyId);
}
