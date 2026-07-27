package com.thiagsilvadev.erpapi.inventory.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    Optional<InventoryItem> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    Optional<InventoryItem> findByCompanyIdAndProductIdAndWarehouseId(UUID companyId, UUID productId, UUID warehouseId);

    Optional<InventoryItem> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<InventoryItem> findAllByCompanyId(UUID companyId, Pageable pageable);

    @Query(
            value =
                    """
                    select count(*) as "itemCount",
                           coalesce(sum(item.quantity_on_hand), 0) as "quantityOnHand",
                           coalesce(sum(item.reserved_quantity), 0) as "reservedQuantity",
                           coalesce(sum(item.quantity_on_hand - item.reserved_quantity), 0) as "availableQuantity",
                           count(*) filter (where item.quantity_on_hand - item.reserved_quantity = 0)
                                as "outOfStockItems"
                      from inventory_items item
                     where item.company_id = :companyId
                    """,
            nativeQuery = true)
    InventoryKpiRow findKpiRow(@Param("companyId") UUID companyId);

    interface InventoryKpiRow {
        long getItemCount();

        long getQuantityOnHand();

        long getReservedQuantity();

        long getAvailableQuantity();

        long getOutOfStockItems();
    }
}
