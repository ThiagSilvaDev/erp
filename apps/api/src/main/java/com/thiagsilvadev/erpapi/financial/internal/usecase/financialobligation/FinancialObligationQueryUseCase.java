package com.thiagsilvadev.erpapi.financial.internal.usecase.financialobligation;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.financialobligation.FinancialObligationResponse;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialObligation;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialObligationRepository;

@Component
public class FinancialObligationQueryUseCase {

    private final FinancialObligationRepository financialObligationRepository;
    private final FinancialMapper mapper;

    public FinancialObligationQueryUseCase(
            FinancialObligationRepository financialObligationRepository, FinancialMapper mapper) {
        this.financialObligationRepository = financialObligationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public FinancialObligationResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(financialObligationRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(FinancialObligation.class, id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<FinancialObligationResponse> list(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return financialObligationRepository
                .findAllByCompanyId(companyId, pageable)
                .map(mapper::toResponse);
    }
}
