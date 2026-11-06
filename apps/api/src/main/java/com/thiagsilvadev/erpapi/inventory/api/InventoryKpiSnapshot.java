package com.thiagsilvadev.erpapi.inventory.api;

import java.time.LocalDate;
import java.util.List;

public record InventoryKpiSnapshot(
        Current current, Movements currentMovements, Movements previousMovements, List<Trend> trend) {

    public InventoryKpiSnapshot {
        trend = List.copyOf(trend);
    }

    public record Current(
            long itemCount, long quantityOnHand, long reservedQuantity, long availableQuantity, long outOfStockItems) {}

    public record Movements(long inbound, long outbound, long reserved, long released, long adjusted) {}

    public record Trend(
            LocalDate from, LocalDate to, long inbound, long outbound, long reserved, long released, long adjusted) {}
}
