package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingentry;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceCriteria;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccountType;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingEntryRepository;

@Component
public class ListAccountBalancesUseCase {

    private final AccountingEntryRepository entryRepository;

    public ListAccountBalancesUseCase(AccountingEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public List<AccountBalanceResponse> execute(AccountBalanceCriteria criteria) {
        var companyId = TenantContext.requireTenantId();
        return entryRepository
                .findAccountBalances(
                        companyId,
                        criteria.from(),
                        criteria.to(),
                        criteria.currencyCode().toUpperCase())
                .stream()
                .map(row -> new AccountBalanceResponse(
                        row.getAccountId(),
                        row.getAccountCode(),
                        row.getAccountName(),
                        AccountingAccountType.valueOf(row.getAccountType()),
                        row.getCurrencyCode(),
                        row.getAmount()))
                .toList();
    }
}
