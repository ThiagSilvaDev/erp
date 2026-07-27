package com.thiagsilvadev.erpapi.financial.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;

class FinancialKpiCalculatorTest {

    @Test
    void shouldCalculatePeriodsReceivablesAndCurrencyTrends() {
        var invoices = List.of(
                new FinancialKpiCalculator.InvoiceRow(date("2026-06-30"), "BRL", 1, amount("100")),
                new FinancialKpiCalculator.InvoiceRow(date("2026-07-01"), "BRL", 2, amount("350")));
        var collections = List.of(new FinancialKpiCalculator.CollectionRow(date("2026-07-01"), "BRL", amount("150")));
        var receivables = List.of(new FinancialKpiCalculator.ReceivableRow("BRL", amount("200"), amount("50")));

        var result = new FinancialKpiCalculator().calculate(invoices, collections, receivables, period());

        assertThat(result.current().invoices()).isEqualTo(2);
        assertThat(result.current().invoiced().get("BRL")).isEqualByComparingTo("350");
        assertThat(result.previous().invoiced().get("BRL")).isEqualByComparingTo("100");
        assertThat(result.receivables().open().get("BRL")).isEqualByComparingTo("200");
        assertThat(result.receivables().overdue().get("BRL")).isEqualByComparingTo("50");
        assertThat(result.trend()).hasSize(2);
        assertThat(result.trend().get(1).collected().get("BRL")).isEqualByComparingTo("0");
    }

    private static ComparativePeriod period() {
        return new ComparativePeriod(
                range("2026-07-01", "2026-07-02"),
                range("2026-06-29", "2026-06-30"),
                List.of(range("2026-07-01", "2026-07-01"), range("2026-07-02", "2026-07-02")));
    }

    private static DateRange range(String from, String to) {
        return new DateRange(date(from), date(to));
    }

    private static LocalDate date(String value) {
        return LocalDate.parse(value);
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}
