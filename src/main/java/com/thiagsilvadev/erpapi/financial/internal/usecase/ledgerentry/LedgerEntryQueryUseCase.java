package com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.ledgerentry.LedgerEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;

@Component
public class LedgerEntryQueryUseCase {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final FinancialMapper mapper;

    public LedgerEntryQueryUseCase(LedgerEntryRepository ledgerEntryRepository, FinancialMapper mapper) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public LedgerEntryResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(ledgerEntryRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(LedgerEntry.class, id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<LedgerEntryResponse> list(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return ledgerEntryRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
