package com.thiagsilvadev.erpapi.inventory.api;

import java.time.LocalDate;
import java.util.List;

public record InventoryKpiSnapshot(Current current, List<DailyMovement> movements) {

    public InventoryKpiSnapshot {
        movements = List.copyOf(movements);
    }

    public record Current(
            long itemCount, long quantityOnHand, long reservedQuantity, long availableQuantity, long outOfStockItems) {}

    public record DailyMovement(
            LocalDate date, long inbound, long outbound, long reserved, long released, long adjusted) {}
}
