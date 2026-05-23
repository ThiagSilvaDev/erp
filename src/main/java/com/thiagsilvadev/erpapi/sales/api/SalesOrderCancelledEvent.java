package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public record SalesOrderCancelledEvent(UUID salesOrderId) {

    public static SalesOrderCancelledEvent cancelled(UUID salesOrderId) {
        return new SalesOrderCancelledEvent(salesOrderId);
    }
}
