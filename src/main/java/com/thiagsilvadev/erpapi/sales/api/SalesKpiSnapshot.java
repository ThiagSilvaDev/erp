package com.thiagsilvadev.erpapi.sales.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record SalesKpiSnapshot(Totals current, Totals previous, List<Trend> trend) {

    public SalesKpiSnapshot {
        trend = List.copyOf(trend);
    }

    public record Totals(
            long orders,
            long confirmedOrders,
            long cancelledOrders,
            BigDecimal cancellationPercentage,
            Map<String, BigDecimal> confirmedRevenue,
            Map<String, BigDecimal> averageOrderValue) {

        public Totals {
            confirmedRevenue = Map.copyOf(confirmedRevenue);
            averageOrderValue = Map.copyOf(averageOrderValue);
        }
    }

    public record Trend(
            LocalDate from,
            LocalDate to,
            long orders,
            long confirmedOrders,
            long cancelledOrders,
            Map<String, BigDecimal> confirmedRevenue) {

        public Trend {
            confirmedRevenue = Map.copyOf(confirmedRevenue);
        }
    }
}
