package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public record SalesOrderItemSnapshot(
        UUID productId, UUID warehouseId, String productSku, String productName, int quantity) {}
