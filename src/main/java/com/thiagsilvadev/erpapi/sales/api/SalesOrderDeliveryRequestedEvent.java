package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public record SalesOrderDeliveryRequestedEvent(UUID salesOrderId) {

    public static SalesOrderDeliveryRequestedEvent requested(UUID salesOrderId) {
        return new SalesOrderDeliveryRequestedEvent(salesOrderId);
    }
}
