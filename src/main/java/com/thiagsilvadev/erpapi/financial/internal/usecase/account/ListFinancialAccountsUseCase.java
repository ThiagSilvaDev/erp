package com.thiagsilvadev.erpapi.financial.internal.usecase.account;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.account.FinancialAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialAccountRepository;

@Component
public class ListFinancialAccountsUseCase {

    private final FinancialAccountRepository accountRepository;
    private final FinancialMapper mapper;

    public ListFinancialAccountsUseCase(FinancialAccountRepository accountRepository, FinancialMapper mapper) {
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<FinancialAccountResponse> execute(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return accountRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
