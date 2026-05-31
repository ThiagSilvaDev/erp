package com.thiagsilvadev.erpapi.inventory.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    boolean existsByCode(String code);

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    Optional<Warehouse> findByCode(String code);

    Optional<Warehouse> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<Warehouse> findByCompanyIdAndCode(UUID companyId, String code);

    Page<Warehouse> findAllByCompanyId(UUID companyId, Pageable pageable);
}
