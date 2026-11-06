package com.thiagsilvadev.erpapi.kpi.internal.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.common.web.exception.ProblemDetailFactory;
import com.thiagsilvadev.erpapi.common.web.exception.ValidationExceptionHandler;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;
import com.thiagsilvadev.erpapi.kpi.internal.usecase.ExecutiveKpiResponseMapper;
import com.thiagsilvadev.erpapi.kpi.internal.usecase.GetExecutiveKpisUseCase;
import com.thiagsilvadev.erpapi.kpi.internal.usecase.KpiPeriodResolver;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiSnapshot;

class KpiControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(UUID.randomUUID());
        var useCase = new GetExecutiveKpisUseCase(
                new KpiPeriodResolver(Clock.systemUTC()),
                (companyId, period) -> emptySales(),
                (companyId, period, asOf) -> emptyFinancial(),
                (companyId, period) -> emptyInventory(),
                (companyId, period) -> emptyLogistics(),
                new ExecutiveKpiResponseMapper());
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = standaloneSetup(new KpiController(useCase))
                .setValidator(validator)
                .setControllerAdvice(new ValidationExceptionHandler(new ProblemDetailFactory(Clock.systemUTC())))
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldBindExplicitPeriodAndBucket() throws Exception {
        mockMvc.perform(get("/api/v1/kpis/executive")
                        .queryParam("from", "2026-07-01")
                        .queryParam("to", "2026-07-31")
                        .queryParam("bucket", "WEEK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.from").value("2026-07-01"))
                .andExpect(jsonPath("$.period.to").value("2026-07-31"))
                .andExpect(jsonPath("$.period.bucket").value("WEEK"))
                .andExpect(jsonPath("$.sales.metrics").exists())
                .andExpect(jsonPath("$.sales.cards").doesNotExist());
    }

    @Test
    void shouldRejectPartialPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/kpis/executive").queryParam("from", "2026-07-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnsupportedBucket() throws Exception {
        mockMvc.perform(get("/api/v1/kpis/executive").queryParam("bucket", "QUARTER"))
                .andExpect(status().isBadRequest());
    }

    private static SalesKpiSnapshot emptySales() {
        var totals = new SalesKpiSnapshot.Totals(0, 0, 0, null, Map.of(), Map.of());
        return new SalesKpiSnapshot(totals, totals, List.of());
    }

    private static FinancialKpiSnapshot emptyFinancial() {
        var totals = new FinancialKpiSnapshot.Totals(0, Map.of(), Map.of());
        return new FinancialKpiSnapshot(
                totals, totals, new FinancialKpiSnapshot.Receivables(Map.of(), Map.of()), List.of());
    }

    private static InventoryKpiSnapshot emptyInventory() {
        var movements = new InventoryKpiSnapshot.Movements(0, 0, 0, 0, 0);
        return new InventoryKpiSnapshot(
                new InventoryKpiSnapshot.Current(0, 0, 0, 0, 0), movements, movements, List.of());
    }

    private static LogisticsKpiSnapshot emptyLogistics() {
        var totals = new LogisticsKpiSnapshot.Totals(0, 0, 0, null, null);
        return new LogisticsKpiSnapshot(new LogisticsKpiSnapshot.Current(0, 0, 0, 0), totals, totals, List.of());
    }
}
