package com.thiagsilvadev.erpapi.financial.internal.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.reporting.DateRange;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiSnapshot;

@Component
class FinancialKpiCalculator {

    FinancialKpiSnapshot calculate(
            List<InvoiceRow> invoices,
            List<CollectionRow> collections,
            List<ReceivableRow> receivables,
            ComparativePeriod period) {
        Totals current = totals(invoices, collections, period.current());
        Totals previous = totals(invoices, collections, period.previous());
        var receivableTotals = receivables(receivables);
        var trend = period.trendRanges().stream()
                .map(range -> trend(invoices, collections, range, current))
                .toList();
        return new FinancialKpiSnapshot(current.snapshot(), previous.snapshot(), receivableTotals, trend);
    }

    private static FinancialKpiSnapshot.Trend trend(
            List<InvoiceRow> invoices, List<CollectionRow> collections, DateRange range, Totals current) {
        Totals totals = totals(invoices, collections, range);
        return new FinancialKpiSnapshot.Trend(
                range.from(),
                range.to(),
                totals.invoices,
                values(totals.invoiced, current.invoiced.keySet()),
                values(totals.collected, current.collected.keySet()));
    }

    private static Totals totals(List<InvoiceRow> invoices, List<CollectionRow> collections, DateRange range) {
        Totals totals = new Totals();
        invoices.stream().filter(row -> range.contains(row.date())).forEach(totals::add);
        collections.stream().filter(row -> range.contains(row.date())).forEach(totals::add);
        return totals;
    }

    private static FinancialKpiSnapshot.Receivables receivables(List<ReceivableRow> rows) {
        Map<String, BigDecimal> open = new HashMap<>();
        Map<String, BigDecimal> overdue = new HashMap<>();
        rows.forEach(row -> {
            open.merge(row.currencyCode(), row.openAmount(), BigDecimal::add);
            overdue.merge(row.currencyCode(), row.overdueAmount(), BigDecimal::add);
        });
        return new FinancialKpiSnapshot.Receivables(open, overdue);
    }

    private static Map<String, BigDecimal> values(Map<String, BigDecimal> source, java.util.Set<String> currencies) {
        Map<String, BigDecimal> result = new HashMap<>();
        currencies.forEach(currency -> result.put(currency, source.getOrDefault(currency, BigDecimal.ZERO)));
        return result;
    }

    record InvoiceRow(LocalDate date, String currencyCode, long count, BigDecimal amount) {}

    record CollectionRow(LocalDate date, String currencyCode, BigDecimal amount) {}

    record ReceivableRow(String currencyCode, BigDecimal openAmount, BigDecimal overdueAmount) {}

    private static final class Totals {
        private long invoices;
        private final Map<String, BigDecimal> invoiced = new HashMap<>();
        private final Map<String, BigDecimal> collected = new HashMap<>();

        private void add(InvoiceRow row) {
            invoices += row.count();
            invoiced.merge(row.currencyCode(), row.amount(), BigDecimal::add);
        }

        private void add(CollectionRow row) {
            collected.merge(row.currencyCode(), row.amount(), BigDecimal::add);
        }

        private FinancialKpiSnapshot.Totals snapshot() {
            return new FinancialKpiSnapshot.Totals(invoices, invoiced, collected);
        }
    }
}
