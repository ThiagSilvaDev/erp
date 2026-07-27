package com.thiagsilvadev.erpapi.kpi.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ExecutiveKpiResponse(
        Instant generatedAt,
        Period period,
        Sales sales,
        Financial financial,
        Inventory inventory,
        Logistics logistics) {

    public record Period(
            LocalDate from,
            LocalDate to,
            LocalDate previousFrom,
            LocalDate previousTo,
            KpiBucket bucket,
            String timezone) {}

    public record CountComparison(long current, long previous, BigDecimal percentageChange) {}

    public record DecimalComparison(BigDecimal current, BigDecimal previous, BigDecimal percentageChange) {}

    public record RateComparison(
            BigDecimal currentPercentage, BigDecimal previousPercentage, BigDecimal percentagePointChange) {}

    public record MoneyComparison(
            String currencyCode, BigDecimal current, BigDecimal previous, BigDecimal percentageChange) {}

    public record MoneyValue(String currencyCode, BigDecimal amount) {}

    public record Sales(SalesCards cards, List<SalesTrend> trend) {}

    public record SalesCards(
            CountComparison orders,
            CountComparison confirmedOrders,
            CountComparison cancelledOrders,
            RateComparison cancellationRate,
            List<MoneyComparison> confirmedRevenue,
            List<MoneyComparison> averageOrderValue) {}

    public record SalesTrend(
            LocalDate from,
            LocalDate to,
            long orders,
            long confirmedOrders,
            long cancelledOrders,
            List<MoneyValue> confirmedRevenue) {}

    public record Financial(FinancialCards cards, List<FinancialTrend> trend) {}

    public record FinancialCards(
            CountComparison invoices,
            List<MoneyComparison> invoiced,
            List<MoneyComparison> collected,
            List<MoneyValue> openReceivables,
            List<MoneyValue> overdueReceivables) {}

    public record FinancialTrend(
            LocalDate from, LocalDate to, long invoices, List<MoneyValue> invoiced, List<MoneyValue> collected) {}

    public record Inventory(InventoryCurrent current, InventoryMovementCards movements, List<InventoryTrend> trend) {}

    public record InventoryCurrent(
            long itemCount, long quantityOnHand, long reservedQuantity, long availableQuantity, long outOfStockItems) {}

    public record InventoryMovementCards(
            CountComparison inbound,
            CountComparison outbound,
            CountComparison reserved,
            CountComparison released,
            CountComparison adjusted) {}

    public record InventoryTrend(
            LocalDate from, LocalDate to, long inbound, long outbound, long reserved, long released, long adjusted) {}

    public record Logistics(LogisticsCurrent current, LogisticsCards cards, List<LogisticsTrend> trend) {}

    public record LogisticsCurrent(long created, long dispatchAssigned, long loaded, long inTransit) {}

    public record LogisticsCards(
            CountComparison created,
            CountComparison delivered,
            CountComparison cancelled,
            RateComparison deliveryRate,
            DecimalComparison averageDeliveryHours) {}

    public record LogisticsTrend(
            LocalDate from,
            LocalDate to,
            long created,
            long delivered,
            long cancelled,
            BigDecimal averageDeliveryHours) {}
}
