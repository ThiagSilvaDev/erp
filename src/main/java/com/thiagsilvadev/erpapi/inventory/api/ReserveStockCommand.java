package com.thiagsilvadev.erpapi.inventory.api;

import java.util.UUID;

public record ReserveStockCommand(UUID companyId, UUID productId, UUID warehouseId, int quantity, UUID salesOrderId) {}
