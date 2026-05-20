package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public record SalesOrderConfirmedItem(
        UUID productId, UUID warehouseId, String productSku, String productName, int quantity) {}
