package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.financial.api.FinancialKpiSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;
import com.thiagsilvadev.erpapi.kpi.internal.dto.ExecutiveKpiResponse;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiSnapshot;

@Component
public class ExecutiveKpiResponseMapper {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public ExecutiveKpiResponseMapper() {}

    ExecutiveKpiResponse toResponse(
            KpiPeriodResolver.KpiPeriod period,
            SalesKpiSnapshot sales,
            FinancialKpiSnapshot financial,
            InventoryKpiSnapshot inventory,
            LogisticsKpiSnapshot logistics) {
        return new ExecutiveKpiResponse(
                period.generatedAt(),
                new ExecutiveKpiResponse.Period(
                        period.from(), period.to(), period.previousFrom(), period.previousTo(), period.bucket(), "UTC"),
                sales(sales),
                financial(financial),
                inventory(inventory),
                logistics(logistics));
    }

    private static ExecutiveKpiResponse.Sales sales(SalesKpiSnapshot snapshot) {
        var current = snapshot.current();
        var previous = snapshot.previous();
        var metrics = new ExecutiveKpiResponse.SalesMetrics(
                countComparison(current.orders(), previous.orders()),
                countComparison(current.confirmedOrders(), previous.confirmedOrders()),
                countComparison(current.cancelledOrders(), previous.cancelledOrders()),
                rateComparison(current.cancellationPercentage(), previous.cancellationPercentage()),
                moneyComparison(current.confirmedRevenue(), previous.confirmedRevenue()),
                moneyComparison(current.averageOrderValue(), previous.averageOrderValue()));
        var trend = snapshot.trend().stream()
                .map(point -> new ExecutiveKpiResponse.SalesTrend(
                        point.from(),
                        point.to(),
                        point.orders(),
                        point.confirmedOrders(),
                        point.cancelledOrders(),
                        moneyValues(point.confirmedRevenue())))
                .toList();
        return new ExecutiveKpiResponse.Sales(metrics, trend);
    }

    private static ExecutiveKpiResponse.Financial financial(FinancialKpiSnapshot snapshot) {
        var current = snapshot.current();
        var previous = snapshot.previous();
        var metrics = new ExecutiveKpiResponse.FinancialMetrics(
                countComparison(current.invoices(), previous.invoices()),
                moneyComparison(current.invoiced(), previous.invoiced()),
                moneyComparison(current.collected(), previous.collected()),
                moneyValues(snapshot.receivables().open()),
                moneyValues(snapshot.receivables().overdue()));
        var trend = snapshot.trend().stream()
                .map(point -> new ExecutiveKpiResponse.FinancialTrend(
                        point.from(),
                        point.to(),
                        point.invoices(),
                        moneyValues(point.invoiced()),
                        moneyValues(point.collected())))
                .toList();
        return new ExecutiveKpiResponse.Financial(metrics, trend);
    }

    private static ExecutiveKpiResponse.Inventory inventory(InventoryKpiSnapshot snapshot) {
        var current = snapshot.current();
        var currentMovements = snapshot.currentMovements();
        var previousMovements = snapshot.previousMovements();
        var currentResponse = new ExecutiveKpiResponse.InventoryCurrent(
                current.itemCount(),
                current.quantityOnHand(),
                current.reservedQuantity(),
                current.availableQuantity(),
                current.outOfStockItems());
        var metrics = new ExecutiveKpiResponse.InventoryMovementMetrics(
                countComparison(currentMovements.inbound(), previousMovements.inbound()),
                countComparison(currentMovements.outbound(), previousMovements.outbound()),
                countComparison(currentMovements.reserved(), previousMovements.reserved()),
                countComparison(currentMovements.released(), previousMovements.released()),
                countComparison(currentMovements.adjusted(), previousMovements.adjusted()));
        var trend = snapshot.trend().stream()
                .map(point -> new ExecutiveKpiResponse.InventoryTrend(
                        point.from(),
                        point.to(),
                        point.inbound(),
                        point.outbound(),
                        point.reserved(),
                        point.released(),
                        point.adjusted()))
                .toList();
        return new ExecutiveKpiResponse.Inventory(currentResponse, metrics, trend);
    }

    private static ExecutiveKpiResponse.Logistics logistics(LogisticsKpiSnapshot snapshot) {
        var current = snapshot.current();
        var currentTotals = snapshot.currentTotals();
        var previousTotals = snapshot.previousTotals();
        var currentResponse = new ExecutiveKpiResponse.LogisticsCurrent(
                current.created(), current.dispatchAssigned(), current.loaded(), current.inTransit());
        var metrics = new ExecutiveKpiResponse.LogisticsMetrics(
                countComparison(currentTotals.created(), previousTotals.created()),
                countComparison(currentTotals.delivered(), previousTotals.delivered()),
                countComparison(currentTotals.cancelled(), previousTotals.cancelled()),
                rateComparison(currentTotals.deliveryPercentage(), previousTotals.deliveryPercentage()),
                decimalComparison(currentTotals.averageDeliveryHours(), previousTotals.averageDeliveryHours()));
        var trend = snapshot.trend().stream()
                .map(point -> new ExecutiveKpiResponse.LogisticsTrend(
                        point.from(),
                        point.to(),
                        point.created(),
                        point.delivered(),
                        point.cancelled(),
                        point.averageDeliveryHours()))
                .toList();
        return new ExecutiveKpiResponse.Logistics(currentResponse, metrics, trend);
    }

    private static ExecutiveKpiResponse.CountComparison countComparison(long current, long previous) {
        return new ExecutiveKpiResponse.CountComparison(
                current, previous, percentageChange(BigDecimal.valueOf(current), BigDecimal.valueOf(previous)));
    }

    private static ExecutiveKpiResponse.DecimalComparison decimalComparison(BigDecimal current, BigDecimal previous) {
        return new ExecutiveKpiResponse.DecimalComparison(
                current, previous, current == null || previous == null ? null : percentageChange(current, previous));
    }

    private static ExecutiveKpiResponse.RateComparison rateComparison(BigDecimal current, BigDecimal previous) {
        return new ExecutiveKpiResponse.RateComparison(
                current, previous, current == null || previous == null ? null : scale(current.subtract(previous)));
    }

    private static java.util.List<ExecutiveKpiResponse.MoneyComparison> moneyComparison(
            Map<String, BigDecimal> current, Map<String, BigDecimal> previous) {
        return unionKeys(current, previous).stream()
                .map(currency -> {
                    BigDecimal currentAmount = current.getOrDefault(currency, BigDecimal.ZERO);
                    BigDecimal previousAmount = previous.getOrDefault(currency, BigDecimal.ZERO);
                    return new ExecutiveKpiResponse.MoneyComparison(
                            currency,
                            money(currentAmount),
                            money(previousAmount),
                            percentageChange(currentAmount, previousAmount));
                })
                .toList();
    }

    private static java.util.List<ExecutiveKpiResponse.MoneyValue> moneyValues(Map<String, BigDecimal> values) {
        return moneyValues(values, values.keySet());
    }

    private static java.util.List<ExecutiveKpiResponse.MoneyValue> moneyValues(
            Map<String, BigDecimal> values, Collection<String> currencies) {
        return currencies.stream()
                .sorted()
                .map(currency -> new ExecutiveKpiResponse.MoneyValue(
                        currency, money(values.getOrDefault(currency, BigDecimal.ZERO))))
                .toList();
    }

    private static SortedSet<String> unionKeys(Map<String, BigDecimal> current, Map<String, BigDecimal> previous) {
        SortedSet<String> currencies = new TreeSet<>(current.keySet());
        currencies.addAll(previous.keySet());
        return currencies;
    }

    private static BigDecimal percentageChange(BigDecimal current, BigDecimal previous) {
        if (previous.signum() == 0) {
            return null;
        }
        return scale(current.subtract(previous).multiply(ONE_HUNDRED).divide(previous, 8, RoundingMode.HALF_UP));
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
