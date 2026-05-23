package com.thiagsilvadev.erpapi.sales.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SalesOrderDeliveryRequestedEvent(
        UUID salesOrderId,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        String routeName,
        UUID originWarehouseId,
        String destinationDescription,
        List<Item> items,
        Instant occurredAt) {

    public static SalesOrderDeliveryRequestedEvent requested(
            UUID salesOrderId,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            String routeName,
            UUID originWarehouseId,
            String destinationDescription,
            List<Item> items,
            Instant occurredAt) {
        return new SalesOrderDeliveryRequestedEvent(
                salesOrderId,
                sellerCompanyId,
                buyerCompanyId,
                customerId,
                routeName,
                originWarehouseId,
                destinationDescription,
                items,
                occurredAt);
    }

    public record Item(UUID productId, UUID warehouseId, String sku, String name, int quantity) {

        public static Item created(UUID productId, UUID warehouseId, String sku, String name, int quantity) {
            return new Item(productId, warehouseId, sku, name, quantity);
        }
    }
}
