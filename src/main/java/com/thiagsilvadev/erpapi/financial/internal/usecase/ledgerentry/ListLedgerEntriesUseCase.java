package com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.ledgerentry.LedgerEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;

@Component
public class ListLedgerEntriesUseCase {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final FinancialMapper mapper;

    public ListLedgerEntriesUseCase(LedgerEntryRepository ledgerEntryRepository, FinancialMapper mapper) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<LedgerEntryResponse> execute(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return ledgerEntryRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
