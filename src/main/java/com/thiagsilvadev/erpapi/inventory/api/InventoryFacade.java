package com.thiagsilvadev.erpapi.inventory.api;

import java.util.UUID;

public interface InventoryFacade {

    WarehouseSnapshot resolveWarehouse(UUID companyId, String code);

    InventoryReservationSnapshot reserveStock(ReserveStockCommand command);

    void releaseReservationsForSalesOrder(UUID companyId, UUID salesOrderId);

    void consumeReservationsForSalesOrder(UUID companyId, UUID salesOrderId);
}
