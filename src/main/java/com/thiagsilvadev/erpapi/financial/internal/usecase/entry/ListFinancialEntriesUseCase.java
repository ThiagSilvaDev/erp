package com.thiagsilvadev.erpapi.financial.internal.usecase.entry;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.entry.FinancialEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;

@Component
public class ListFinancialEntriesUseCase {

    private final FinancialEntryRepository entryRepository;
    private final FinancialMapper mapper;

    public ListFinancialEntriesUseCase(FinancialEntryRepository entryRepository, FinancialMapper mapper) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<FinancialEntryResponse> execute(UUID accountId, Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        if (accountId != null) {
            return entryRepository
                    .findByCompanyIdAndAccountId(companyId, accountId, pageable)
                    .map(mapper::toResponse);
        }
        return entryRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
