package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
    private static final Instant QUERY_FROM = Instant.parse("2026-06-29T00:00:00Z");
    private static final Instant QUERY_TO = Instant.parse("2026-07-03T00:00:00Z");

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldAggregateTenantScopedCardsComparisonsAndGapFilledTrends() {
        TenantContext.setTenantId(COMPANY_ID);
        AtomicReference<QueryCall> call = new AtomicReference<>();
        SalesKpiFacade salesFacade = (companyId, from, to) -> {
            call.set(new QueryCall(companyId, from, to));
            return new SalesKpiSnapshot(List.of(
                    new SalesKpiSnapshot.Daily(date("2026-06-30"), "BRL", 2, 1, 1, decimal("100.00")),
                    new SalesKpiSnapshot.Daily(date("2026-07-01"), "BRL", 3, 2, 1, decimal("300.00"))));
        };
        FinancialKpiFacade financialFacade = (companyId, from, to, asOf) -> new FinancialKpiSnapshot(
                List.of(new FinancialKpiSnapshot.DailyInvoice(date("2026-07-01"), "BRL", 2, decimal("350.00"))),
                List.of(new FinancialKpiSnapshot.DailyCollection(date("2026-07-01"), "BRL", decimal("150.00"))),
                List.of(new FinancialKpiSnapshot.Receivable("BRL", decimal("200.00"), decimal("50.00"))));
        InventoryKpiFacade inventoryFacade = (companyId, from, to) -> new InventoryKpiSnapshot(
                new InventoryKpiSnapshot.Current(4, 100, 20, 80, 1),
                List.of(new InventoryKpiSnapshot.DailyMovement(date("2026-07-01"), 10, 3, 2, 1, 4)));
        LogisticsKpiFacade logisticsFacade = (companyId, from, to) -> new LogisticsKpiSnapshot(
                new LogisticsKpiSnapshot.Current(1, 2, 3, 4),
                List.of(new LogisticsKpiSnapshot.Daily(date("2026-07-01"), 2, 1, 1, decimal("6.00"), 1)));

        var response = useCase(salesFacade, financialFacade, inventoryFacade, logisticsFacade)
                .execute(new KpiCriteria(date("2026-07-01"), date("2026-07-02"), KpiBucket.DAY));

        assertThat(response.period().timezone()).isEqualTo("UTC");
        assertThat(response.sales().cards().orders().current()).isEqualTo(3);
        assertThat(response.sales().cards().orders().previous()).isEqualTo(2);
        assertThat(response.sales().cards().orders().percentageChange()).isEqualByComparingTo("50.00");
        assertThat(response.sales().cards().cancellationRate().currentPercentage())
                .isEqualByComparingTo("33.33");
        assertThat(response.sales().cards().cancellationRate().percentagePointChange())
                .isEqualByComparingTo("-16.67");
        assertThat(response.sales().cards().confirmedRevenue().getFirst().percentageChange())
                .isEqualByComparingTo("200.00");
        assertThat(response.sales().cards().averageOrderValue().getFirst().current())
                .isEqualByComparingTo("150.00");
        assertThat(response.sales().trend()).hasSize(2);
        assertThat(response.sales().trend().get(1).orders()).isZero();
        assertThat(response.sales().trend().get(1).confirmedRevenue().getFirst().amount())
                .isEqualByComparingTo("0.00");
        assertThat(response.financial().cards().openReceivables().getFirst().amount())
                .isEqualByComparingTo("200.00");
        assertThat(response.inventory().current().availableQuantity()).isEqualTo(80);
        assertThat(response.logistics().cards().deliveryRate().currentPercentage())
                .isEqualByComparingTo("50.00");
        assertThat(response.logistics().cards().averageDeliveryHours().current())
                .isEqualByComparingTo("6.00");
        assertThat(call.get()).isEqualTo(new QueryCall(COMPANY_ID, QUERY_FROM, QUERY_TO));
    }

    @Test
    void shouldReturnUndefinedChangesAndRatesForEmptyData() {
        TenantContext.setTenantId(COMPANY_ID);
        var response = emptyUseCase().execute(new KpiCriteria(date("2026-07-01"), date("2026-07-02"), KpiBucket.DAY));

        assertThat(response.sales().cards().orders().percentageChange()).isNull();
        assertThat(response.sales().cards().cancellationRate().currentPercentage())
                .isNull();
        assertThat(response.sales().cards().confirmedRevenue()).isEmpty();
        assertThat(response.logistics().cards().averageDeliveryHours().current())
                .isNull();
    }

    @Test
    void shouldClipWeekAndMonthBucketsToTheRequestedRange() {
        TenantContext.setTenantId(COMPANY_ID);

        var weekly = emptyUseCase().execute(new KpiCriteria(date("2026-07-02"), date("2026-07-15"), KpiBucket.WEEK));
        assertThat(weekly.sales().trend())
                .extracting(point -> point.from() + "/" + point.to())
                .containsExactly("2026-07-02/2026-07-05", "2026-07-06/2026-07-12", "2026-07-13/2026-07-15");

        var monthly = emptyUseCase().execute(new KpiCriteria(date("2026-01-15"), date("2026-03-02"), KpiBucket.MONTH));
        assertThat(monthly.sales().trend())
                .extracting(point -> point.from() + "/" + point.to())
                .containsExactly("2026-01-15/2026-01-31", "2026-02-01/2026-02-28", "2026-03-01/2026-03-02");
    }

    private GetExecutiveKpisUseCase useCase(
            SalesKpiFacade salesFacade,
            FinancialKpiFacade financialFacade,
            InventoryKpiFacade inventoryFacade,
            LogisticsKpiFacade logisticsFacade) {
        Clock clock = Clock.fixed(Instant.parse("2026-07-27T15:30:00Z"), ZoneOffset.UTC);
        return new GetExecutiveKpisUseCase(
                new KpiPeriodResolver(clock), salesFacade, financialFacade, inventoryFacade, logisticsFacade);
    }

    private GetExecutiveKpisUseCase emptyUseCase() {
        return useCase(
                (companyId, from, to) -> new SalesKpiSnapshot(List.of()),
                (companyId, from, to, asOf) -> new FinancialKpiSnapshot(List.of(), List.of(), List.of()),
                (companyId, from, to) ->
                        new InventoryKpiSnapshot(new InventoryKpiSnapshot.Current(0, 0, 0, 0, 0), List.of()),
                (companyId, from, to) ->
                        new LogisticsKpiSnapshot(new LogisticsKpiSnapshot.Current(0, 0, 0, 0), List.of()));
    }

    private static LocalDate date(String value) {
        return LocalDate.parse(value);
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private record QueryCall(UUID companyId, Instant from, Instant to) {}
}
