package com.thiagsilvadev.erpapi.sales.internal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiSnapshot;

@Component
class SalesKpiCalculator {

    SalesKpiSnapshot calculate(List<DailyRow> rows, ComparativePeriod period) {
        Totals current = totals(rows, period.current());
        Totals previous = totals(rows, period.previous());
        List<SalesKpiSnapshot.Trend> trend = period.trendRanges().stream()
                .map(range -> trend(rows, range, current.revenue.keySet()))
                .toList();
        return new SalesKpiSnapshot(current.snapshot(), previous.snapshot(), trend);
    }

    private static SalesKpiSnapshot.Trend trend(
            List<DailyRow> rows, DateRange range, java.util.Set<String> currencies) {
        Totals totals = totals(rows, range);
        Map<String, BigDecimal> revenue = new HashMap<>();
        currencies.forEach(currency -> revenue.put(currency, totals.revenue.getOrDefault(currency, BigDecimal.ZERO)));
        return new SalesKpiSnapshot.Trend(
                range.from(), range.to(), totals.orders, totals.confirmed, totals.cancelled, revenue);
    }

    private static Totals totals(List<DailyRow> rows, DateRange range) {
        Totals totals = new Totals();
        rows.stream().filter(row -> range.contains(row.date())).forEach(totals::add);
        return totals;
    }

    record DailyRow(
            LocalDate date,
            String currencyCode,
            long orders,
            long confirmedOrders,
            long cancelledOrders,
            BigDecimal confirmedRevenue) {}

    private static final class Totals {
        private long orders;
        private long confirmed;
        private long cancelled;
        private final Map<String, BigDecimal> revenue = new HashMap<>();
        private final Map<String, Long> confirmedByCurrency = new HashMap<>();

        private void add(DailyRow row) {
            orders += row.orders();
            confirmed += row.confirmedOrders();
            cancelled += row.cancelledOrders();
            revenue.merge(row.currencyCode(), row.confirmedRevenue(), BigDecimal::add);
            confirmedByCurrency.merge(row.currencyCode(), row.confirmedOrders(), Long::sum);
        }

        private SalesKpiSnapshot.Totals snapshot() {
            Map<String, BigDecimal> averages = new HashMap<>();
            revenue.forEach((currency, amount) -> {
                long count = confirmedByCurrency.getOrDefault(currency, 0L);
                if (count > 0) {
                    averages.put(currency, amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
                }
            });
            long completed = confirmed + cancelled;
            BigDecimal cancellationPercentage = completed == 0
                    ? null
                    : BigDecimal.valueOf(cancelled)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP);
            return new SalesKpiSnapshot.Totals(orders, confirmed, cancelled, cancellationPercentage, revenue, averages);
        }
    }
}
