package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount.AccountingAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccount;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingAccountRepository;

@Component
public class GetAccountingAccountByIdUseCase {

    private final AccountingAccountRepository accountRepository;
    private final FinancialMapper mapper;

    public GetAccountingAccountByIdUseCase(AccountingAccountRepository accountRepository, FinancialMapper mapper) {
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.financial.api.FinancialPermissions).READ)")
    public AccountingAccountResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(accountRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(AccountingAccount.class, id)));
    }
}
