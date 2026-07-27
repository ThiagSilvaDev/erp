package com.thiagsilvadev.erpapi.kpi.internal.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.time.Clock;
import java.util.List;
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
                (companyId, from, to) -> new SalesKpiSnapshot(List.of()),
                (companyId, from, to, asOf) -> new FinancialKpiSnapshot(List.of(), List.of(), List.of()),
                (companyId, from, to) ->
                        new InventoryKpiSnapshot(new InventoryKpiSnapshot.Current(0, 0, 0, 0, 0), List.of()),
                (companyId, from, to) ->
                        new LogisticsKpiSnapshot(new LogisticsKpiSnapshot.Current(0, 0, 0, 0), List.of()));
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
                .andExpect(jsonPath("$.period.bucket").value("WEEK"));
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
}
