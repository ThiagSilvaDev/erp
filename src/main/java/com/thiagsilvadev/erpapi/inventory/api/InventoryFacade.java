package com.thiagsilvadev.erpapi.inventory.api;

import java.util.Optional;
import java.util.UUID;

public interface InventoryFacade {

    Optional<InventoryItemSnapshot> findItem(UUID productId, UUID warehouseId);

    InventoryReservationSnapshot reserve(UUID productId, UUID warehouseId, int quantity, UUID salesOrderId);

    void releaseBySalesOrderId(UUID salesOrderId);
}
