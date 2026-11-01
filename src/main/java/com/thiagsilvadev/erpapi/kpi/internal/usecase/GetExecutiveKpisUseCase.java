package com.thiagsilvadev.erpapi.kpi.internal.usecase;

import static com.thiagsilvadev.erpapi.kpi.api.KpiPermissions.READ;

import java.time.ZoneOffset;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.api.FinancialKpiFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiFacade;
import com.thiagsilvadev.erpapi.kpi.internal.dto.ExecutiveKpiResponse;
import com.thiagsilvadev.erpapi.kpi.internal.dto.KpiCriteria;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsKpiFacade;
import com.thiagsilvadev.erpapi.sales.api.SalesKpiFacade;

@Component
public class GetExecutiveKpisUseCase {

    private final KpiPeriodResolver periodResolver;
    private final SalesKpiFacade salesFacade;
    private final FinancialKpiFacade financialFacade;
    private final InventoryKpiFacade inventoryFacade;
    private final LogisticsKpiFacade logisticsFacade;
    private final ExecutiveKpiResponseMapper responseMapper;

    public GetExecutiveKpisUseCase(
            KpiPeriodResolver periodResolver,
            SalesKpiFacade salesFacade,
            FinancialKpiFacade financialFacade,
            InventoryKpiFacade inventoryFacade,
            LogisticsKpiFacade logisticsFacade,
            ExecutiveKpiResponseMapper responseMapper) {
        this.periodResolver = periodResolver;
        this.salesFacade = salesFacade;
        this.financialFacade = financialFacade;
        this.inventoryFacade = inventoryFacade;
        this.logisticsFacade = logisticsFacade;
        this.responseMapper = responseMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public ExecutiveKpiResponse execute(KpiCriteria criteria) {
        var period = periodResolver.resolve(criteria);
        var comparativePeriod = period.comparativePeriod();
        var companyId = TenantContext.requireTenantId();
        var sales = salesFacade.calculate(companyId, comparativePeriod);
        var financial = financialFacade.calculate(
                companyId,
                comparativePeriod,
                period.generatedAt().atZone(ZoneOffset.UTC).toLocalDate());
        var inventory = inventoryFacade.calculate(companyId, comparativePeriod);
        var logistics = logisticsFacade.calculate(companyId, comparativePeriod);
        return responseMapper.toResponse(period, sales, financial, inventory, logistics);
    }
}
