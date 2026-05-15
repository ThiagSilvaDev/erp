package com.thiagsilvadev.erpapi.inventory.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    boolean existsByCode(String code);

    Optional<Warehouse> findByCode(String code);
}
