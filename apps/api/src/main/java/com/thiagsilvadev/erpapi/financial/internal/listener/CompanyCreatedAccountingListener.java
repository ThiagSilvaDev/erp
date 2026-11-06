package com.thiagsilvadev.erpapi.financial.internal.listener;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.company.api.CompanyCreatedEvent;
import com.thiagsilvadev.erpapi.financial.internal.service.ChartOfAccountsService;

@Component
class CompanyCreatedAccountingListener {

    private final ChartOfAccountsService chartOfAccountsService;

    CompanyCreatedAccountingListener(ChartOfAccountsService chartOfAccountsService) {
        this.chartOfAccountsService = chartOfAccountsService;
    }

    @ApplicationModuleListener
    void on(CompanyCreatedEvent event) {
        chartOfAccountsService.ensureDefaultAccounts(event.companyId());
    }
}
