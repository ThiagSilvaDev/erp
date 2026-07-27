package com.thiagsilvadev.erpapi.sales.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesKpiSnapshot(List<Daily> daily) {

    public SalesKpiSnapshot {
        daily = List.copyOf(daily);
    }

    public record Daily(
            LocalDate date,
            String currencyCode,
            long orders,
            long confirmedOrders,
            long cancelledOrders,
            BigDecimal confirmedRevenue) {}
}
