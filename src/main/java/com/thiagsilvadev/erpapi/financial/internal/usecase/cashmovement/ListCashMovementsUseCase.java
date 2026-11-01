package com.thiagsilvadev.erpapi.financial.internal.usecase.cashmovement;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.cashmovement.CashMovementResponse;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.repository.CashMovementRepository;

@Component
public class ListCashMovementsUseCase {

    private final CashMovementRepository cashMovementRepository;
    private final FinancialMapper mapper;

    public ListCashMovementsUseCase(CashMovementRepository cashMovementRepository, FinancialMapper mapper) {
        this.cashMovementRepository = cashMovementRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<CashMovementResponse> execute(UUID accountId, Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        if (accountId != null) {
            return cashMovementRepository
                    .findByCompanyIdAndAccountId(companyId, accountId, pageable)
                    .map(mapper::toResponse);
        }
        return cashMovementRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
