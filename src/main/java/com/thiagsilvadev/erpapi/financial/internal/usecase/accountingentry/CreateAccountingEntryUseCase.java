package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingentry;

import static com.thiagsilvadev.erpapi.financial.api.FinancialPermissions.WRITE;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.CreateAccountingEntryRequest;
import com.thiagsilvadev.erpapi.financial.internal.mapper.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccount;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntry;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntrySourceType;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingAccountRepository;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingEntryRepository;

@Component
public class CreateAccountingEntryUseCase {

    private final AccountingEntryRepository entryRepository;
    private final AccountingAccountRepository accountRepository;
    private final FinancialMapper mapper;

    public CreateAccountingEntryUseCase(
            AccountingEntryRepository entryRepository,
            AccountingAccountRepository accountRepository,
            FinancialMapper mapper) {
        this.entryRepository = entryRepository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public AccountingEntryResponse execute(CreateAccountingEntryRequest request) {
        var companyId = TenantContext.requireTenantId();
        AccountingAccount account = accountRepository
                .findByIdAndCompanyId(request.accountId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(AccountingAccount.class, request.accountId()));
        if (!account.canAcceptPostings()) {
            throw new ConflictException("Accounting account cannot accept postings.");
        }

        AccountingEntry entry = entryRepository.save(AccountingEntry.create(
                companyId,
                account.getId(),
                request.competenceDate(),
                request.amount(),
                request.currencyCode(),
                request.direction(),
                AccountingEntrySourceType.MANUAL,
                null,
                request.description()));
        return mapper.toResponse(entry);
    }
}
