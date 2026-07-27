package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiFacade;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiBucket;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiCriteria;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiFacade;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiSnapshot;

class GetExecutiveKpisUseCaseTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldOrchestrateModuleReportsAndMapExecutiveResponse() {
        TenantContext.setTenantId(COMPANY_ID);
        AtomicReference<ComparativePeriod> capturedPeriod = new AtomicReference<>();
        SalesKpiFacade salesFacade = (companyId, period) -> {
            assertThat(companyId).isEqualTo(COMPANY_ID);
            capturedPeriod.set(period);
            return salesReport();
        };
        FinancialKpiFacade financialFacade = (companyId, period, asOf) -> financialReport();
        InventoryKpiFacade inventoryFacade = (companyId, period) -> inventoryReport();
        LogisticsKpiFacade logisticsFacade = (companyId, period) -> logisticsReport();

        var response = useCase(salesFacade, financialFacade, inventoryFacade, logisticsFacade)
                .execute(new KpiCriteria(date("2026-07-01"), date("2026-07-02"), KpiBucket.DAY));

        assertThat(capturedPeriod.get().queryFromInclusive()).isEqualTo(Instant.parse("2026-06-29T00:00:00Z"));
        assertThat(capturedPeriod.get().queryToExclusive()).isEqualTo(Instant.parse("2026-07-03T00:00:00Z"));
        assertThat(response.period().timezone()).isEqualTo("UTC");
        assertThat(response.sales().metrics().orders().percentageChange()).isEqualByComparingTo("50.00");
        assertThat(response.sales().metrics().cancellationRate().percentagePointChange())
                .isEqualByComparingTo("-16.67");
        assertThat(response.financial().metrics().openReceivables().getFirst().amount())
                .isEqualByComparingTo("200.00");
        assertThat(response.inventory().current().availableQuantity()).isEqualTo(80);
        assertThat(response.logistics().metrics().deliveryRate().currentPercentage())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldReturnUndefinedGenericChangesForEmptyReports() {
        TenantContext.setTenantId(COMPANY_ID);

        var response = useCase(
                        (companyId, period) -> emptySales(),
                        (companyId, period, asOf) -> emptyFinancial(),
                        (companyId, period) -> emptyInventory(),
                        (companyId, period) -> emptyLogistics())
                .execute(new KpiCriteria(date("2026-07-01"), date("2026-07-02"), KpiBucket.DAY));

        assertThat(response.sales().metrics().orders().percentageChange()).isNull();
        assertThat(response.sales().metrics().cancellationRate().currentPercentage())
                .isNull();
        assertThat(response.sales().metrics().confirmedRevenue()).isEmpty();
        assertThat(response.logistics().metrics().averageDeliveryHours().current())
                .isNull();
    }

    private GetExecutiveKpisUseCase useCase(
            SalesKpiFacade salesFacade,
            FinancialKpiFacade financialFacade,
            InventoryKpiFacade inventoryFacade,
            LogisticsKpiFacade logisticsFacade) {
        Clock clock = Clock.fixed(Instant.parse("2026-07-27T15:30:00Z"), ZoneOffset.UTC);
        return new GetExecutiveKpisUseCase(
                new KpiPeriodResolver(clock),
                salesFacade,
                financialFacade,
                inventoryFacade,
                logisticsFacade,
                new ExecutiveKpiResponseMapper());
    }

    private static SalesKpiSnapshot salesReport() {
        return new SalesKpiSnapshot(
                new SalesKpiSnapshot.Totals(
                        3, 2, 1, decimal("33.33"), Map.of("BRL", decimal("300")), Map.of("BRL", decimal("150"))),
                new SalesKpiSnapshot.Totals(
                        2, 1, 1, decimal("50"), Map.of("BRL", decimal("100")), Map.of("BRL", decimal("100"))),
                List.of(new SalesKpiSnapshot.Trend(
                        date("2026-07-01"), date("2026-07-01"), 3, 2, 1, Map.of("BRL", decimal("300")))));
    }

    private static FinancialKpiSnapshot financialReport() {
        return new FinancialKpiSnapshot(
                new FinancialKpiSnapshot.Totals(2, Map.of("BRL", decimal("350")), Map.of("BRL", decimal("150"))),
                new FinancialKpiSnapshot.Totals(0, Map.of(), Map.of()),
                new FinancialKpiSnapshot.Receivables(Map.of("BRL", decimal("200")), Map.of("BRL", decimal("50"))),
                List.of());
    }

    private static InventoryKpiSnapshot inventoryReport() {
        return new InventoryKpiSnapshot(
                new InventoryKpiSnapshot.Current(4, 100, 20, 80, 1),
                new InventoryKpiSnapshot.Movements(10, 3, 2, 1, 4),
                new InventoryKpiSnapshot.Movements(0, 0, 0, 0, 0),
                List.of());
    }

    private static LogisticsKpiSnapshot logisticsReport() {
        return new LogisticsKpiSnapshot(
                new LogisticsKpiSnapshot.Current(1, 2, 3, 4),
                new LogisticsKpiSnapshot.Totals(2, 1, 1, decimal("50"), decimal("6")),
                new LogisticsKpiSnapshot.Totals(0, 0, 0, null, null),
                List.of());
    }

    static SalesKpiSnapshot emptySales() {
        var totals = new SalesKpiSnapshot.Totals(0, 0, 0, null, Map.of(), Map.of());
        return new SalesKpiSnapshot(totals, totals, List.of());
    }

    static FinancialKpiSnapshot emptyFinancial() {
        var totals = new FinancialKpiSnapshot.Totals(0, Map.of(), Map.of());
        return new FinancialKpiSnapshot(
                totals, totals, new FinancialKpiSnapshot.Receivables(Map.of(), Map.of()), List.of());
    }

    static InventoryKpiSnapshot emptyInventory() {
        var movements = new InventoryKpiSnapshot.Movements(0, 0, 0, 0, 0);
        return new InventoryKpiSnapshot(
                new InventoryKpiSnapshot.Current(0, 0, 0, 0, 0), movements, movements, List.of());
    }

    static LogisticsKpiSnapshot emptyLogistics() {
        var totals = new LogisticsKpiSnapshot.Totals(0, 0, 0, null, null);
        return new LogisticsKpiSnapshot(new LogisticsKpiSnapshot.Current(0, 0, 0, 0), totals, totals, List.of());
    }

    private static LocalDate date(String value) {
        return LocalDate.parse(value);
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
