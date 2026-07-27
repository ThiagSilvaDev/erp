package com.thiagsilvadev.erpapi.financial.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FinancialKpiSnapshot(
        List<DailyInvoice> invoices, List<DailyCollection> collections, List<Receivable> receivables) {

    public FinancialKpiSnapshot {
        invoices = List.copyOf(invoices);
        collections = List.copyOf(collections);
        receivables = List.copyOf(receivables);
    }

    public record DailyInvoice(LocalDate date, String currencyCode, long count, BigDecimal amount) {}

    public record DailyCollection(LocalDate date, String currencyCode, BigDecimal amount) {}

    public record Receivable(String currencyCode, BigDecimal openAmount, BigDecimal overdueAmount) {}
}
