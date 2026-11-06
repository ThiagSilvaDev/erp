package com.thiagsilvadev.erpapi.inventory.internal.model;

public enum StockMovementReason {
    INITIAL_STOCK,
    PURCHASE_RECEIPT,
    SALE_ORDER_CONFIRMED,
    SALE_ORDER_CANCELLED,
    SALE_FULFILLED,
    CUSTOMER_RETURN,
    SUPPLIER_RETURN,
    MANUAL_ADJUSTMENT,
    INVENTORY_COUNT,
    DAMAGED_GOODS,
    LOST_GOODS,
    WAREHOUSE_TRANSFER
}
