package com.thiagsilvadev.erpapi.sales.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;

class SalesKpiCalculatorTest {

    @Test
    void shouldCalculateDomainMetricsAndGapFilledTrends() {
        var rows = List.of(row("2026-06-30", "BRL", 2, 1, 1, "100"), row("2026-07-01", "BRL", 3, 2, 1, "300"));

        var result = new SalesKpiCalculator().calculate(rows, period());

        assertThat(result.current().orders()).isEqualTo(3);
        assertThat(result.current().cancellationPercentage()).isEqualByComparingTo("33.33");
        assertThat(result.current().averageOrderValue().get("BRL")).isEqualByComparingTo("150.00");
        assertThat(result.previous().cancellationPercentage()).isEqualByComparingTo("50.00");
        assertThat(result.trend()).hasSize(2);
        assertThat(result.trend().get(1).orders()).isZero();
        assertThat(result.trend().get(1).confirmedRevenue().get("BRL")).isEqualByComparingTo("0");
    }

    private static SalesKpiCalculator.DailyRow row(
            String date, String currency, long orders, long confirmed, long cancelled, String revenue) {
        return new SalesKpiCalculator.DailyRow(
                LocalDate.parse(date), currency, orders, confirmed, cancelled, new BigDecimal(revenue));
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
