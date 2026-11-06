package com.thiagsilvadev.erpapi.financial.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record FinancialKpiSnapshot(Totals current, Totals previous, Receivables receivables, List<Trend> trend) {

    public FinancialKpiSnapshot {
        trend = List.copyOf(trend);
    }

    public record Totals(long invoices, Map<String, BigDecimal> invoiced, Map<String, BigDecimal> collected) {

        public Totals {
            invoiced = Map.copyOf(invoiced);
            collected = Map.copyOf(collected);
        }
    }

    public record Receivables(Map<String, BigDecimal> open, Map<String, BigDecimal> overdue) {

        public Receivables {
            open = Map.copyOf(open);
            overdue = Map.copyOf(overdue);
        }
    }

    public record Trend(
            LocalDate from,
            LocalDate to,
            long invoices,
            Map<String, BigDecimal> invoiced,
            Map<String, BigDecimal> collected) {

        public Trend {
            invoiced = Map.copyOf(invoiced);
            collected = Map.copyOf(collected);
        }
    }
}
