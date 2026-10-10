package com.thiagsilvadev.erpapi.inventory.internal.model;

import java.util.function.ObjIntConsumer;

public enum StockMovementType {
    IN(true, InventoryItem::increase),
    OUT(false, InventoryItem::decrease),
    RESERVE(false, InventoryItem::reserve),
    RELEASE_RESERVATION(false, InventoryItem::release),
    TRANSFER_IN(true, InventoryItem::increase),
    TRANSFER_OUT(false, InventoryItem::decrease),
    ADJUSTMENT(true, InventoryItem::increase);

    private final boolean canCreateItem;
    private final ObjIntConsumer<InventoryItem> operation;

    StockMovementType(boolean canCreateItem, ObjIntConsumer<InventoryItem> operation) {
        this.canCreateItem = canCreateItem;
        this.operation = operation;
    }

    public boolean canCreateItem() {
        return canCreateItem;
    }

    public void applyTo(InventoryItem item, int quantity) {
        operation.accept(item, quantity);
    }
}
