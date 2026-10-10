package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.financial.api.FinancialKpiSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;
import com.thiagsilvadev.erpapi.kpi.internal.dto.ExecutiveKpiResponse;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiBucket;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiSnapshot;

class ExecutiveKpiResponseMapperTest {

    private final ExecutiveKpiResponseMapper mapper = new ExecutiveKpiResponseMapper();

    @Test
    void shouldMapAllSectionsAndTrends() {
        var date = LocalDate.parse("2026-07-01");
        var salesTrend = new SalesKpiSnapshot.Trend(date, date, 3, 2, 1, Map.of("BRL", decimal("300")));
        var financialTrend = new FinancialKpiSnapshot.Trend(
                date, date, 2, Map.of("BRL", decimal("200")), Map.of("BRL", decimal("100")));
        var inventoryTrend = new InventoryKpiSnapshot.Trend(date, date, 4, 3, 2, 1, 5);
        var logisticsTrend = new LogisticsKpiSnapshot.Trend(date, date, 2, 1, 1, decimal("6"));

        var response = map(
                sales(
                        totals(3, 2, 1, decimal("33.33"), Map.of("BRL", decimal("300")), Map.of()),
                        totals(2, 1, 1, decimal("50"), Map.of(), Map.of()),
                        List.of(salesTrend)),
                financial(
                        new FinancialKpiSnapshot.Totals(
                                2, Map.of("BRL", decimal("200")), Map.of("BRL", decimal("100"))),
                        new FinancialKpiSnapshot.Totals(1, Map.of(), Map.of()),
                        new FinancialKpiSnapshot.Receivables(
                                Map.of("BRL", decimal("80")), Map.of("BRL", decimal("20"))),
                        List.of(financialTrend)),
                new InventoryKpiSnapshot(
                        new InventoryKpiSnapshot.Current(4, 100, 20, 80, 1),
                        new InventoryKpiSnapshot.Movements(4, 3, 2, 1, 5),
                        new InventoryKpiSnapshot.Movements(2, 1, 1, 0, 3),
                        List.of(inventoryTrend)),
                logistics(
                        new LogisticsKpiSnapshot.Current(2, 1, 1, 3),
                        new LogisticsKpiSnapshot.Totals(2, 1, 1, decimal("50"), decimal("6")),
                        new LogisticsKpiSnapshot.Totals(1, 1, 0, decimal("40"), decimal("4")),
                        List.of(logisticsTrend)));

        assertThat(response.sales().trend())
                .containsExactly(new ExecutiveKpiResponse.SalesTrend(
                        date, date, 3, 2, 1, List.of(new ExecutiveKpiResponse.MoneyValue("BRL", decimal("300.00")))));
        assertThat(response.financial().trend()).hasSize(1);
        assertThat(response.inventory().current().availableQuantity()).isEqualTo(80);
        assertThat(response.inventory().trend()).hasSize(1);
        assertThat(response.logistics().current().inTransit()).isEqualTo(3);
        assertThat(response.logistics().trend()).hasSize(1);
    }

    @Test
    void shouldCalculateRelativeAndPercentagePointChangesSeparately() {
        var response = map(
                sales(
                        totals(
                                3,
                                2,
                                0,
                                decimal("66.67"),
                                Map.of("EUR", decimal("50"), "USD", decimal("110.005")),
                                Map.of()),
                        totals(2, 1, 0, decimal("50"), Map.of("EUR", decimal("100"), "USD", decimal("100")), Map.of()),
                        List.of()),
                emptyFinancial(),
                emptyInventory(),
                logistics(
                        new LogisticsKpiSnapshot.Current(3, 2, 1, 1),
                        new LogisticsKpiSnapshot.Totals(3, 2, 1, decimal("50"), decimal("8")),
                        new LogisticsKpiSnapshot.Totals(2, 1, 1, decimal("40"), decimal("4")),
                        List.of()));

        assertThat(response.sales().metrics().orders().percentageChange()).isEqualByComparingTo("50.00");
        assertThat(response.sales().metrics().confirmedRevenue().get(1).current())
                .isEqualByComparingTo("110.01");
        assertThat(response.sales().metrics().confirmedRevenue().getFirst().percentageChange())
                .isEqualByComparingTo("-50.00");
        assertThat(response.sales().metrics().confirmedRevenue().get(1).percentageChange())
                .isEqualByComparingTo("10.01");
        assertThat(response.sales().metrics().cancellationRate().percentagePointChange())
                .isEqualByComparingTo("16.67");
        assertThat(response.logistics().metrics().deliveryRate().percentagePointChange())
                .isEqualByComparingTo("10.00");
        assertThat(response.logistics().metrics().averageDeliveryHours().percentageChange())
                .isEqualByComparingTo("100.00");
    }

    @Test
    void shouldReturnNullForZeroBaselinesAndNullableComparisons() {
        var response = map(
                sales(
                        totals(4, 0, 0, null, Map.of("EUR", decimal("10")), Map.of()),
                        totals(0, 0, 0, null, Map.of("EUR", BigDecimal.ZERO), Map.of()),
                        List.of()),
                emptyFinancial(),
                emptyInventory(),
                logistics(
                        new LogisticsKpiSnapshot.Current(0, 0, 0, 0),
                        new LogisticsKpiSnapshot.Totals(0, 0, 0, null, null),
                        new LogisticsKpiSnapshot.Totals(0, 0, 0, null, null),
                        List.of()));

        assertThat(response.sales().metrics().orders().percentageChange()).isNull();
        assertThat(response.sales().metrics().cancellationRate().percentagePointChange())
                .isNull();
        assertThat(response.sales().metrics().confirmedRevenue().getFirst().percentageChange())
                .isNull();
        assertThat(response.logistics().metrics().deliveryRate().percentagePointChange())
                .isNull();
        assertThat(response.logistics().metrics().averageDeliveryHours().current())
                .isNull();
        assertThat(response.logistics().metrics().averageDeliveryHours().percentageChange())
                .isNull();
    }

    @Test
    void shouldUnionCurrenciesSortThemAndDefaultMissingAmountsToZero() {
        var response = map(
                emptySales(),
                financial(
                        new FinancialKpiSnapshot.Totals(0, Map.of("USD", decimal("12"), "EUR", decimal("8")), Map.of()),
                        new FinancialKpiSnapshot.Totals(0, Map.of("CAD", decimal("5"), "USD", decimal("12")), Map.of()),
                        new FinancialKpiSnapshot.Receivables(Map.of(), Map.of()),
                        List.of()),
                emptyInventory(),
                emptyLogistics());

        var comparisons = response.financial().metrics().invoiced();
        assertThat(comparisons).extracting("currencyCode").containsExactly("CAD", "EUR", "USD");
        assertThat(comparisons.get(0).current()).isEqualByComparingTo("0.00");
        assertThat(comparisons.get(1).previous()).isEqualByComparingTo("0.00");
        assertThat(comparisons.get(2).percentageChange()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldCalculatePercentageFromUnroundedAmountsAndRoundHalfUpForDisplay() {
        var response = map(
                sales(
                        totals(0, 0, 0, null, Map.of("USD", decimal("1.005")), Map.of()),
                        totals(0, 0, 0, null, Map.of("USD", decimal("1.004")), Map.of()),
                        List.of()),
                emptyFinancial(),
                emptyInventory(),
                emptyLogistics());

        var revenue = response.sales().metrics().confirmedRevenue().getFirst();
        assertThat(revenue.current()).isEqualByComparingTo("1.01");
        assertThat(revenue.previous()).isEqualByComparingTo("1.00");
        assertThat(revenue.percentageChange()).isEqualByComparingTo("0.10");
    }

    @Test
    void shouldMapEmptyReportsToEmptyCollectionsAndUndefinedComparisons() {
        var response = map(emptySales(), emptyFinancial(), emptyInventory(), emptyLogistics());

        assertThat(response.sales().trend()).isEmpty();
        assertThat(response.sales().metrics().orders().percentageChange()).isNull();
        assertThat(response.financial().trend()).isEmpty();
        assertThat(response.inventory().trend()).isEmpty();
        assertThat(response.logistics().trend()).isEmpty();
        assertThat(response.logistics().metrics().averageDeliveryHours().current())
                .isNull();
    }

    private ExecutiveKpiResponse map(
            SalesKpiSnapshot sales,
            FinancialKpiSnapshot financial,
            InventoryKpiSnapshot inventory,
            LogisticsKpiSnapshot logistics) {
        var date = LocalDate.parse("2026-07-01");
        var period = new KpiPeriodResolver.KpiPeriod(
                Instant.parse("2026-07-02T00:00:00Z"),
                date,
                date.plusDays(1),
                date.minusDays(2),
                date.minusDays(1),
                KpiBucket.DAY);
        return mapper.toResponse(period, sales, financial, inventory, logistics);
    }

    private static SalesKpiSnapshot sales(
            SalesKpiSnapshot.Totals current, SalesKpiSnapshot.Totals previous, List<SalesKpiSnapshot.Trend> trend) {
        return new SalesKpiSnapshot(current, previous, trend);
    }

    private static SalesKpiSnapshot.Totals totals(
            long orders,
            long confirmed,
            long cancelled,
            BigDecimal cancellationRate,
            Map<String, BigDecimal> revenue,
            Map<String, BigDecimal> averageOrderValue) {
        return new SalesKpiSnapshot.Totals(orders, confirmed, cancelled, cancellationRate, revenue, averageOrderValue);
    }

    private static FinancialKpiSnapshot financial(
            FinancialKpiSnapshot.Totals current,
            FinancialKpiSnapshot.Totals previous,
            FinancialKpiSnapshot.Receivables receivables,
            List<FinancialKpiSnapshot.Trend> trend) {
        return new FinancialKpiSnapshot(current, previous, receivables, trend);
    }

    private static LogisticsKpiSnapshot logistics(
            LogisticsKpiSnapshot.Current current,
            LogisticsKpiSnapshot.Totals currentTotals,
            LogisticsKpiSnapshot.Totals previousTotals,
            List<LogisticsKpiSnapshot.Trend> trend) {
        return new LogisticsKpiSnapshot(current, currentTotals, previousTotals, trend);
    }

    private static SalesKpiSnapshot emptySales() {
        var totals = totals(0, 0, 0, null, Map.of(), Map.of());
        return sales(totals, totals, List.of());
    }

    private static FinancialKpiSnapshot emptyFinancial() {
        var totals = new FinancialKpiSnapshot.Totals(0, Map.of(), Map.of());
        return financial(totals, totals, new FinancialKpiSnapshot.Receivables(Map.of(), Map.of()), List.of());
    }

    private static InventoryKpiSnapshot emptyInventory() {
        var movements = new InventoryKpiSnapshot.Movements(0, 0, 0, 0, 0);
        return new InventoryKpiSnapshot(
                new InventoryKpiSnapshot.Current(0, 0, 0, 0, 0), movements, movements, List.of());
    }

    private static LogisticsKpiSnapshot emptyLogistics() {
        var totals = new LogisticsKpiSnapshot.Totals(0, 0, 0, null, null);
        return logistics(new LogisticsKpiSnapshot.Current(0, 0, 0, 0), totals, totals, List.of());
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
