package com.thiagsilvadev.erpapi.inventory.api;

import java.util.Optional;
import java.util.UUID;

public interface InventoryFacade {

    Optional<InventoryItemSnapshot> findItem(UUID productId, UUID warehouseId);

    Optional<InventoryItemSnapshot> findItem(UUID companyId, UUID productId, UUID warehouseId);

    Optional<WarehouseSnapshot> findWarehouseByCode(String code);

    Optional<WarehouseSnapshot> findWarehouseByCode(UUID companyId, String code);

    InventoryReservationSnapshot reserve(UUID productId, UUID warehouseId, int quantity, UUID salesOrderId);

    InventoryReservationSnapshot reserve(
            UUID companyId, UUID productId, UUID warehouseId, int quantity, UUID salesOrderId);

    void releaseBySalesOrderId(UUID salesOrderId);

    void releaseBySalesOrderId(UUID companyId, UUID salesOrderId);

    void consumeReservationsForSalesOrder(UUID salesOrderId);

    void consumeReservationsForSalesOrder(UUID companyId, UUID salesOrderId);
}
