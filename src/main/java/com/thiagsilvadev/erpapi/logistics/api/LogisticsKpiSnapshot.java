package com.thiagsilvadev.erpapi.logistics.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LogisticsKpiSnapshot(Current current, Totals currentTotals, Totals previousTotals, List<Trend> trend) {

    public LogisticsKpiSnapshot {
        trend = List.copyOf(trend);
    }

    public record Current(long created, long dispatchAssigned, long loaded, long inTransit) {}

    public record Totals(
            long created,
            long delivered,
            long cancelled,
            BigDecimal deliveryPercentage,
            BigDecimal averageDeliveryHours) {}

    public record Trend(
            LocalDate from,
            LocalDate to,
            long created,
            long delivered,
            long cancelled,
            BigDecimal averageDeliveryHours) {}
}
