package com.thiagsilvadev.erpapi.inventory.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    Optional<InventoryItem> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    Optional<InventoryItem> findByCompanyIdAndProductIdAndWarehouseId(UUID companyId, UUID productId, UUID warehouseId);

    Optional<InventoryItem> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<InventoryItem> findAllByCompanyId(UUID companyId, Pageable pageable);
}
