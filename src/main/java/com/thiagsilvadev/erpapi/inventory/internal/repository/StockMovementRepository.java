package com.thiagsilvadev.erpapi.inventory.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Optional<StockMovement> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<StockMovement> findAllByCompanyId(UUID companyId, Pageable pageable);
}
