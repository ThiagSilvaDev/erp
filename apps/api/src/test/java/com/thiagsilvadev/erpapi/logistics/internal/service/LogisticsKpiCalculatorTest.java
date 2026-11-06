package com.thiagsilvadev.erpapi.logistics.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;

class LogisticsKpiCalculatorTest {

    @Test
    void shouldCalculateDeliveryRateAndWeightedAverageDuration() {
        var rows = List.of(
                row("2026-06-30", 2, 1, 1, "10", 1),
                row("2026-07-01", 4, 2, 1, "18", 2),
                row("2026-07-02", 1, 0, 0, "0", 0));
        var current = new LogisticsKpiSnapshot.Current(1, 2, 3, 4);

        var result = new LogisticsKpiCalculator().calculate(current, rows, period());

        assertThat(result.currentTotals().deliveryPercentage()).isEqualByComparingTo("66.67");
        assertThat(result.currentTotals().averageDeliveryHours()).isEqualByComparingTo("9.00");
        assertThat(result.previousTotals().deliveryPercentage()).isEqualByComparingTo("50.00");
        assertThat(result.trend()).hasSize(2);
        assertThat(result.trend().get(1).averageDeliveryHours()).isNull();
    }

    private static LogisticsKpiCalculator.DailyRow row(
            String date, long created, long delivered, long cancelled, String hours, long durations) {
        return new LogisticsKpiCalculator.DailyRow(
                LocalDate.parse(date), created, delivered, cancelled, new BigDecimal(hours), durations);
    }

    private static ComparativePeriod period() {
        return new ComparativePeriod(
                range("2026-07-01", "2026-07-02"),
                range("2026-06-29", "2026-06-30"),
                List.of(range("2026-07-01", "2026-07-01"), range("2026-07-02", "2026-07-02")));
    }

    private static DateRange range(String from, String to) {
        return new DateRange(LocalDate.parse(from), LocalDate.parse(to));
    }
}
