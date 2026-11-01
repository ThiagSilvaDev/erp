package com.thiagsilvadev.erpapi.financial.internal.usecase.account;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.account.FinancialAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccount;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialAccountRepository;

@Component
public class GetFinancialAccountByIdUseCase {

    private final FinancialAccountRepository accountRepository;
    private final FinancialMapper mapper;

    public GetFinancialAccountByIdUseCase(FinancialAccountRepository accountRepository, FinancialMapper mapper) {
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public FinancialAccountResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(accountRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(FinancialAccount.class, id)));
    }
}
