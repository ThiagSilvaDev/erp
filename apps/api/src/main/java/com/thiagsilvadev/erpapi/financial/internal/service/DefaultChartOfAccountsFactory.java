package com.thiagsilvadev.erpapi.financial.internal.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccount;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccountType;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingSystemRole;

@Component
class DefaultChartOfAccountsFactory {

    List<AccountingAccount> createDefault(UUID companyId) {
        return List.of(
                account(
                        companyId,
                        "3.1",
                        "Receita Bruta de Vendas",
                        "Receita operacional de vendas",
                        AccountingAccountType.REVENUE,
                        AccountingSystemRole.REVENUE_SALES),
                account(
                        companyId,
                        "4.1",
                        "CMV",
                        "Custo das mercadorias vendidas",
                        AccountingAccountType.EXPENSE,
                        AccountingSystemRole.COGS),
                account(
                        companyId,
                        "5.1",
                        "Despesas Administrativas",
                        null,
                        AccountingAccountType.EXPENSE,
                        AccountingSystemRole.ADMIN_EXPENSE),
                account(
                        companyId,
                        "5.2",
                        "Despesas com Vendas",
                        null,
                        AccountingAccountType.EXPENSE,
                        AccountingSystemRole.SALES_EXPENSE),
                account(
                        companyId,
                        "6.1",
                        "Receitas Financeiras",
                        null,
                        AccountingAccountType.REVENUE,
                        AccountingSystemRole.FINANCIAL_REVENUE),
                account(
                        companyId,
                        "6.2",
                        "Despesas Financeiras",
                        null,
                        AccountingAccountType.EXPENSE,
                        AccountingSystemRole.FINANCIAL_EXPENSE));
    }

    private static AccountingAccount account(
            UUID companyId,
            String code,
            String name,
            String description,
            AccountingAccountType type,
            AccountingSystemRole systemRole) {
        return AccountingAccount.create(companyId, code, name, description, null, type, systemRole, true);
    }
}
