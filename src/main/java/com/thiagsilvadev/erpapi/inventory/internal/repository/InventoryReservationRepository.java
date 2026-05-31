package com.thiagsilvadev.erpapi.inventory.internal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryReservation;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

    List<InventoryReservation> findBySalesOrderId(UUID salesOrderId);

    List<InventoryReservation> findByCompanyIdAndSalesOrderId(UUID companyId, UUID salesOrderId);

    java.util.Optional<InventoryReservation> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<InventoryReservation> findAllByCompanyId(UUID companyId, Pageable pageable);
}
