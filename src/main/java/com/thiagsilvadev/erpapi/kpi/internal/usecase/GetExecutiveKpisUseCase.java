package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiFacade;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;
import com.thiagsilvadev.erpapi.kpi.internal.dto.ExecutiveKpiResponse;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiCriteria;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiFacade;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiSnapshot;

@Component
public class GetExecutiveKpisUseCase {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final KpiPeriodResolver periodResolver;
    private final SalesKpiFacade salesFacade;
    private final FinancialKpiFacade financialFacade;
    private final InventoryKpiFacade inventoryFacade;
    private final LogisticsKpiFacade logisticsFacade;

    public GetExecutiveKpisUseCase(
            KpiPeriodResolver periodResolver,
            SalesKpiFacade salesFacade,
            FinancialKpiFacade financialFacade,
            InventoryKpiFacade inventoryFacade,
            LogisticsKpiFacade logisticsFacade) {
        this.periodResolver = periodResolver;
        this.salesFacade = salesFacade;
        this.financialFacade = financialFacade;
        this.inventoryFacade = inventoryFacade;
        this.logisticsFacade = logisticsFacade;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.kpi.api.KpiPermissions).READ)")
    public ExecutiveKpiResponse execute(KpiCriteria criteria) {
        var period = periodResolver.resolve(criteria);
        var companyId = TenantContext.requireTenantId();
        var sales = salesFacade.load(companyId, period.queryFrom(), period.queryToExclusive());
        var financial = financialFacade.load(
                companyId,
                period.queryFrom(),
                period.queryToExclusive(),
                period.generatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate());
        var inventory = inventoryFacade.load(companyId, period.queryFrom(), period.queryToExclusive());
        var logistics = logisticsFacade.load(companyId, period.queryFrom(), period.queryToExclusive());

        return new ExecutiveKpiResponse(
                period.generatedAt(),
                new ExecutiveKpiResponse.Period(
                        period.from(), period.to(), period.previousFrom(), period.previousTo(), period.bucket(), "UTC"),
                sales(period, sales),
                financial(period, financial),
                inventory(period, inventory),
                logistics(period, logistics));
    }

    private ExecutiveKpiResponse.Sales sales(KpiPeriodResolver.KpiPeriod period, SalesKpiSnapshot snapshot) {
        SalesTotals current = salesTotals(snapshot.daily(), period.from(), period.to());
        SalesTotals previous = salesTotals(snapshot.daily(), period.previousFrom(), period.previousTo());
        var cards = new ExecutiveKpiResponse.SalesCards(
                countComparison(current.orders, previous.orders),
                countComparison(current.confirmed, previous.confirmed),
                countComparison(current.cancelled, previous.cancelled),
                rateComparison(
                        current.cancelled,
                        current.confirmed + current.cancelled,
                        previous.cancelled,
                        previous.confirmed + previous.cancelled),
                moneyComparison(current.revenue, previous.revenue),
                moneyComparison(current.averages(), previous.averages()));

        List<ExecutiveKpiResponse.SalesTrend> trend = buckets(period).stream()
                .map(bucket -> {
                    SalesTotals totals = salesTotals(snapshot.daily(), bucket.from(), bucket.to());
                    return new ExecutiveKpiResponse.SalesTrend(
                            bucket.from(),
                            bucket.to(),
                            totals.orders,
                            totals.confirmed,
                            totals.cancelled,
                            moneyValues(totals.revenue, current.revenue.keySet()));
                })
                .toList();
        return new ExecutiveKpiResponse.Sales(cards, trend);
    }

    private ExecutiveKpiResponse.Financial financial(
            KpiPeriodResolver.KpiPeriod period, FinancialKpiSnapshot snapshot) {
        FinancialTotals current = financialTotals(snapshot, period.from(), period.to());
        FinancialTotals previous = financialTotals(snapshot, period.previousFrom(), period.previousTo());
        Map<String, BigDecimal> open = new HashMap<>();
        Map<String, BigDecimal> overdue = new HashMap<>();
        snapshot.receivables().forEach(receivable -> {
            open.merge(receivable.currencyCode(), receivable.openAmount(), BigDecimal::add);
            overdue.merge(receivable.currencyCode(), receivable.overdueAmount(), BigDecimal::add);
        });
        var cards = new ExecutiveKpiResponse.FinancialCards(
                countComparison(current.invoices, previous.invoices),
                moneyComparison(current.invoiced, previous.invoiced),
                moneyComparison(current.collected, previous.collected),
                moneyValues(open),
                moneyValues(overdue));
        List<ExecutiveKpiResponse.FinancialTrend> trend = buckets(period).stream()
                .map(bucket -> {
                    FinancialTotals totals = financialTotals(snapshot, bucket.from(), bucket.to());
                    return new ExecutiveKpiResponse.FinancialTrend(
                            bucket.from(),
                            bucket.to(),
                            totals.invoices,
                            moneyValues(totals.invoiced, current.invoiced.keySet()),
                            moneyValues(totals.collected, current.collected.keySet()));
                })
                .toList();
        return new ExecutiveKpiResponse.Financial(cards, trend);
    }

    private ExecutiveKpiResponse.Inventory inventory(
            KpiPeriodResolver.KpiPeriod period, InventoryKpiSnapshot snapshot) {
        MovementTotals current = movementTotals(snapshot.movements(), period.from(), period.to());
        MovementTotals previous = movementTotals(snapshot.movements(), period.previousFrom(), period.previousTo());
        var item = snapshot.current();
        var currentSnapshot = new ExecutiveKpiResponse.InventoryCurrent(
                item.itemCount(),
                item.quantityOnHand(),
                item.reservedQuantity(),
                item.availableQuantity(),
                item.outOfStockItems());
        var cards = new ExecutiveKpiResponse.InventoryMovementCards(
                countComparison(current.inbound, previous.inbound),
                countComparison(current.outbound, previous.outbound),
                countComparison(current.reserved, previous.reserved),
                countComparison(current.released, previous.released),
                countComparison(current.adjusted, previous.adjusted));
        List<ExecutiveKpiResponse.InventoryTrend> trend = buckets(period).stream()
                .map(bucket -> {
                    MovementTotals totals = movementTotals(snapshot.movements(), bucket.from(), bucket.to());
                    return new ExecutiveKpiResponse.InventoryTrend(
                            bucket.from(),
                            bucket.to(),
                            totals.inbound,
                            totals.outbound,
                            totals.reserved,
                            totals.released,
                            totals.adjusted);
                })
                .toList();
        return new ExecutiveKpiResponse.Inventory(currentSnapshot, cards, trend);
    }

    private ExecutiveKpiResponse.Logistics logistics(
            KpiPeriodResolver.KpiPeriod period, LogisticsKpiSnapshot snapshot) {
        LogisticsTotals current = logisticsTotals(snapshot.daily(), period.from(), period.to());
        LogisticsTotals previous = logisticsTotals(snapshot.daily(), period.previousFrom(), period.previousTo());
        var active = snapshot.current();
        var currentSnapshot = new ExecutiveKpiResponse.LogisticsCurrent(
                active.created(), active.dispatchAssigned(), active.loaded(), active.inTransit());
        var cards = new ExecutiveKpiResponse.LogisticsCards(
                countComparison(current.created, previous.created),
                countComparison(current.delivered, previous.delivered),
                countComparison(current.cancelled, previous.cancelled),
                rateComparison(
                        current.delivered,
                        current.delivered + current.cancelled,
                        previous.delivered,
                        previous.delivered + previous.cancelled),
                decimalComparison(current.averageHours(), previous.averageHours()));
        List<ExecutiveKpiResponse.LogisticsTrend> trend = buckets(period).stream()
                .map(bucket -> {
                    LogisticsTotals totals = logisticsTotals(snapshot.daily(), bucket.from(), bucket.to());
                    return new ExecutiveKpiResponse.LogisticsTrend(
                            bucket.from(),
                            bucket.to(),
                            totals.created,
                            totals.delivered,
                            totals.cancelled,
                            totals.averageHours());
                })
                .toList();
        return new ExecutiveKpiResponse.Logistics(currentSnapshot, cards, trend);
    }

    private static SalesTotals salesTotals(List<SalesKpiSnapshot.Daily> daily, LocalDate from, LocalDate to) {
        SalesTotals totals = new SalesTotals();
        daily.stream().filter(row -> within(row.date(), from, to)).forEach(totals::add);
        return totals;
    }

    private static FinancialTotals financialTotals(FinancialKpiSnapshot snapshot, LocalDate from, LocalDate to) {
        FinancialTotals totals = new FinancialTotals();
        snapshot.invoices().stream().filter(row -> within(row.date(), from, to)).forEach(totals::add);
        snapshot.collections().stream()
                .filter(row -> within(row.date(), from, to))
                .forEach(totals::add);
        return totals;
    }

    private static MovementTotals movementTotals(
            List<InventoryKpiSnapshot.DailyMovement> daily, LocalDate from, LocalDate to) {
        MovementTotals totals = new MovementTotals();
        daily.stream().filter(row -> within(row.date(), from, to)).forEach(totals::add);
        return totals;
    }

    private static LogisticsTotals logisticsTotals(
            List<LogisticsKpiSnapshot.Daily> daily, LocalDate from, LocalDate to) {
        LogisticsTotals totals = new LogisticsTotals();
        daily.stream().filter(row -> within(row.date(), from, to)).forEach(totals::add);
        return totals;
    }

    private static boolean within(LocalDate date, LocalDate from, LocalDate to) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    private static List<BucketRange> buckets(KpiPeriodResolver.KpiPeriod period) {
        List<BucketRange> buckets = new ArrayList<>();
        LocalDate cursor = period.from();
        while (!cursor.isAfter(period.to())) {
            LocalDate end = period.bucket().end(cursor);
            if (end.isAfter(period.to())) {
                end = period.to();
            }
            buckets.add(new BucketRange(cursor, end));
            cursor = end.plusDays(1);
        }
        return buckets;
    }

    private static ExecutiveKpiResponse.CountComparison countComparison(long current, long previous) {
        return new ExecutiveKpiResponse.CountComparison(
                current, previous, percentageChange(BigDecimal.valueOf(current), BigDecimal.valueOf(previous)));
    }

    private static ExecutiveKpiResponse.DecimalComparison decimalComparison(BigDecimal current, BigDecimal previous) {
        return new ExecutiveKpiResponse.DecimalComparison(
                current, previous, current == null || previous == null ? null : percentageChange(current, previous));
    }

    private static ExecutiveKpiResponse.RateComparison rateComparison(
            long currentNumerator, long currentDenominator, long previousNumerator, long previousDenominator) {
        BigDecimal current = percentage(currentNumerator, currentDenominator);
        BigDecimal previous = percentage(previousNumerator, previousDenominator);
        return new ExecutiveKpiResponse.RateComparison(
                current, previous, current == null || previous == null ? null : scale(current.subtract(previous)));
    }

    private static List<ExecutiveKpiResponse.MoneyComparison> moneyComparison(
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

    private static List<ExecutiveKpiResponse.MoneyValue> moneyValues(Map<String, BigDecimal> values) {
        return moneyValues(values, values.keySet());
    }

    private static List<ExecutiveKpiResponse.MoneyValue> moneyValues(
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

    private static BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return null;
        }
        return scale(BigDecimal.valueOf(numerator)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 8, RoundingMode.HALF_UP));
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

    private record BucketRange(LocalDate from, LocalDate to) {}

    private static final class SalesTotals {
        private long orders;
        private long confirmed;
        private long cancelled;
        private final Map<String, BigDecimal> revenue = new HashMap<>();
        private final Map<String, Long> confirmedByCurrency = new HashMap<>();

        private void add(SalesKpiSnapshot.Daily row) {
            orders += row.orders();
            confirmed += row.confirmedOrders();
            cancelled += row.cancelledOrders();
            revenue.merge(row.currencyCode(), row.confirmedRevenue(), BigDecimal::add);
            confirmedByCurrency.merge(row.currencyCode(), row.confirmedOrders(), Long::sum);
        }

        private Map<String, BigDecimal> averages() {
            Map<String, BigDecimal> averages = new HashMap<>();
            revenue.forEach((currency, amount) -> {
                long count = confirmedByCurrency.getOrDefault(currency, 0L);
                if (count > 0) {
                    averages.put(currency, amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
                }
            });
            return averages;
        }
    }

    private static final class FinancialTotals {
        private long invoices;
        private final Map<String, BigDecimal> invoiced = new HashMap<>();
        private final Map<String, BigDecimal> collected = new HashMap<>();

        private void add(FinancialKpiSnapshot.DailyInvoice row) {
            invoices += row.count();
            invoiced.merge(row.currencyCode(), row.amount(), BigDecimal::add);
        }

        private void add(FinancialKpiSnapshot.DailyCollection row) {
            collected.merge(row.currencyCode(), row.amount(), BigDecimal::add);
        }
    }

    private static final class MovementTotals {
        private long inbound;
        private long outbound;
        private long reserved;
        private long released;
        private long adjusted;

        private void add(InventoryKpiSnapshot.DailyMovement row) {
            inbound += row.inbound();
            outbound += row.outbound();
            reserved += row.reserved();
            released += row.released();
            adjusted += row.adjusted();
        }
    }

    private static final class LogisticsTotals {
        private long created;
        private long delivered;
        private long cancelled;
        private BigDecimal deliveryHours = BigDecimal.ZERO;
        private long deliveriesWithDuration;

        private void add(LogisticsKpiSnapshot.Daily row) {
            created += row.created();
            delivered += row.delivered();
            cancelled += row.cancelled();
            deliveryHours = deliveryHours.add(row.deliveryHoursTotal());
            deliveriesWithDuration += row.deliveriesWithDuration();
        }

        private BigDecimal averageHours() {
            return deliveriesWithDuration == 0
                    ? null
                    : deliveryHours.divide(BigDecimal.valueOf(deliveriesWithDuration), 2, RoundingMode.HALF_UP);
        }
    }
}
