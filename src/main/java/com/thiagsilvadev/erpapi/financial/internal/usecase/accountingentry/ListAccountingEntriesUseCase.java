package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingentry;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.DomainValidationException;
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
        boolean hasFrom = criteria.from() != null;
        boolean hasTo = criteria.to() != null;
        if (hasFrom != hasTo) {
            throw new DomainValidationException("Both from and to must be provided together.");
        }
        if (hasFrom && criteria.from().isAfter(criteria.to())) {
            throw new DomainValidationException("from must be on or before to.");
        }

        if (hasFrom && criteria.accountId() != null) {
            return entryRepository
                    .findByCompanyIdAndAccountIdAndCompetenceDateBetween(
                            companyId, criteria.accountId(), criteria.from(), criteria.to(), pageable)
                    .map(mapper::toResponse);
        }
        if (hasFrom) {
            return entryRepository
                    .findByCompanyIdAndCompetenceDateBetween(companyId, criteria.from(), criteria.to(), pageable)
                    .map(mapper::toResponse);
        }
        if (criteria.accountId() != null) {
            return entryRepository
                    .findByCompanyIdAndAccountId(companyId, criteria.accountId(), pageable)
                    .map(mapper::toResponse);
        }
        return entryRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
