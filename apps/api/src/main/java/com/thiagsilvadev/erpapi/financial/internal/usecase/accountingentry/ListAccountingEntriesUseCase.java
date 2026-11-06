package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingentry;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntrySearchCriteria;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingEntryRepository;

@Component
public class ListAccountingEntriesUseCase {

    private final AccountingEntryRepository entryRepository;
    private final FinancialMapper mapper;

    public ListAccountingEntriesUseCase(AccountingEntryRepository entryRepository, FinancialMapper mapper) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<AccountingEntryResponse> execute(AccountingEntrySearchCriteria criteria, Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        boolean hasDateRange = criteria != null && criteria.from() != null;
        boolean hasAccount = criteria != null && criteria.accountId() != null;
        if (hasDateRange && hasAccount) {
            return entryRepository
                    .findByCompanyIdAndAccountIdAndCompetenceDateBetween(
                            companyId, criteria.accountId(), criteria.from(), criteria.to(), pageable)
                    .map(mapper::toResponse);
        }
        if (hasDateRange) {
            return entryRepository
                    .findByCompanyIdAndCompetenceDateBetween(companyId, criteria.from(), criteria.to(), pageable)
                    .map(mapper::toResponse);
        }
        if (hasAccount) {
            return entryRepository
                    .findByCompanyIdAndAccountId(companyId, criteria.accountId(), pageable)
                    .map(mapper::toResponse);
        }
        return entryRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
