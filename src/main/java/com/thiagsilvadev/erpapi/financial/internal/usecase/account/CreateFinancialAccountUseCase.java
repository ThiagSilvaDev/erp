package com.thiagsilvadev.erpapi.financial.internal.usecase.account;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.account.CreateFinancialAccountRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.account.FinancialAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccount;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialAccountRepository;

@Component
public class CreateFinancialAccountUseCase {

    private final FinancialAccountRepository accountRepository;
    private final FinancialMapper mapper;

    public CreateFinancialAccountUseCase(FinancialAccountRepository accountRepository, FinancialMapper mapper) {
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.financial.api.FinancialPermissions).WRITE)")
    public FinancialAccountResponse execute(CreateFinancialAccountRequest request) {
        var companyId = TenantContext.requireTenantId();
        if (accountRepository.existsByCompanyIdAndNameIgnoreCase(
                companyId, request.name().trim())) {
            throw new ConflictException("A financial account with this name already exists.");
        }

        FinancialAccount account = accountRepository.save(
                FinancialAccount.create(companyId, request.name(), request.type(), request.description()));

        return mapper.toResponse(account);
    }
}
