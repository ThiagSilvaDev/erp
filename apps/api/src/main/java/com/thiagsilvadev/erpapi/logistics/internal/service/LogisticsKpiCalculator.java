package com.thiagsilvadev.erpapi.logistics.internal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;

@Component
class LogisticsKpiCalculator {

    LogisticsKpiSnapshot calculate(
            LogisticsKpiSnapshot.Current current, List<DailyRow> rows, ComparativePeriod period) {
        Totals currentTotals = totals(rows, period.current());
        Totals previousTotals = totals(rows, period.previous());
        var trend =
                period.trendRanges().stream().map(range -> trend(rows, range)).toList();
        return new LogisticsKpiSnapshot(current, currentTotals.snapshot(), previousTotals.snapshot(), trend);
    }

    private static LogisticsKpiSnapshot.Trend trend(List<DailyRow> rows, DateRange range) {
        Totals totals = totals(rows, range);
        return new LogisticsKpiSnapshot.Trend(
                range.from(), range.to(), totals.created, totals.delivered, totals.cancelled, totals.averageHours());
    }

    private static Totals totals(List<DailyRow> rows, DateRange range) {
        Totals totals = new Totals();
        rows.stream().filter(row -> range.contains(row.date())).forEach(totals::add);
        return totals;
    }

    record DailyRow(
            LocalDate date,
            long created,
            long delivered,
            long cancelled,
            BigDecimal deliveryHoursTotal,
            long deliveriesWithDuration) {}

    private static final class Totals {
        private long created;
        private long delivered;
        private long cancelled;
        private BigDecimal deliveryHours = BigDecimal.ZERO;
        private long deliveriesWithDuration;

        private void add(DailyRow row) {
            created += row.created();
            delivered += row.delivered();
            cancelled += row.cancelled();
            deliveryHours = deliveryHours.add(row.deliveryHoursTotal());
            deliveriesWithDuration += row.deliveriesWithDuration();
        }

        private LogisticsKpiSnapshot.Totals snapshot() {
            long completed = delivered + cancelled;
            BigDecimal deliveryPercentage = completed == 0
                    ? null
                    : BigDecimal.valueOf(delivered)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP);
            return new LogisticsKpiSnapshot.Totals(created, delivered, cancelled, deliveryPercentage, averageHours());
        }

        private BigDecimal averageHours() {
            return deliveriesWithDuration == 0
                    ? null
                    : deliveryHours.divide(BigDecimal.valueOf(deliveriesWithDuration), 2, RoundingMode.HALF_UP);
        }
    }
}
