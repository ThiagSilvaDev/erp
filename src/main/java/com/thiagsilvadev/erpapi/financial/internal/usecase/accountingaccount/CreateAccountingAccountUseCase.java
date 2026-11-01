package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.WRITE;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount.AccountingAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount.CreateAccountingAccountRequest;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccount;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingAccountRepository;

@Component
public class CreateAccountingAccountUseCase {

    private final AccountingAccountRepository accountRepository;
    private final FinancialMapper mapper;

    public CreateAccountingAccountUseCase(AccountingAccountRepository accountRepository, FinancialMapper mapper) {
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public AccountingAccountResponse execute(CreateAccountingAccountRequest request) {
        var companyId = TenantContext.requireTenantId();
        String code = request.code().trim();
        if (accountRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)) {
            throw new ConflictException("An accounting account with this code already exists.");
        }
        if (request.systemRole() != null
                && accountRepository.existsByCompanyIdAndSystemRole(companyId, request.systemRole())) {
            throw new ConflictException("An accounting account with this system role already exists.");
        }
        if (request.parentId() != null) {
            accountRepository
                    .findByIdAndCompanyId(request.parentId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException(AccountingAccount.class, request.parentId()));
        }

        boolean acceptsPostings = request.acceptsPostings() == null || request.acceptsPostings();
        AccountingAccount account = accountRepository.save(AccountingAccount.create(
                companyId,
                code,
                request.name(),
                request.description(),
                request.parentId(),
                request.type(),
                request.systemRole(),
                acceptsPostings));
        return mapper.toResponse(account);
    }
}
