package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount.AccountingAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccount;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingAccountRepository;

@Component
public class AccountingAccountQueryUseCase {

    private final AccountingAccountRepository accountRepository;
    private final FinancialMapper mapper;

    public AccountingAccountQueryUseCase(AccountingAccountRepository accountRepository, FinancialMapper mapper) {
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public AccountingAccountResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(accountRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(AccountingAccount.class, id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<AccountingAccountResponse> list(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return accountRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
