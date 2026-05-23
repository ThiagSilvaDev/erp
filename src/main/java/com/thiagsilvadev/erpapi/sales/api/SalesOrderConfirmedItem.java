package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public record SalesOrderConfirmedItem(
        UUID productId, UUID warehouseId, String productSku, String productName, int quantity) {

    public static SalesOrderConfirmedItem created(
            UUID productId, UUID warehouseId, String productSku, String productName, int quantity) {
        return new SalesOrderConfirmedItem(productId, warehouseId, productSku, productName, quantity);
    }
}
