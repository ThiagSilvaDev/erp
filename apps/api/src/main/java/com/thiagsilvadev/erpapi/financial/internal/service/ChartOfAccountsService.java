package com.thiagsilvadev.erpapi.financial.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccount;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingSystemRole;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingAccountRepository;

@Service
public class ChartOfAccountsService {

    private final AccountingAccountRepository accountRepository;
    private final DefaultChartOfAccountsFactory factory;

    ChartOfAccountsService(AccountingAccountRepository accountRepository, DefaultChartOfAccountsFactory factory) {
        this.accountRepository = accountRepository;
        this.factory = factory;
    }

    @Transactional
    public void ensureDefaultAccounts(UUID companyId) {
        if (accountRepository.existsByCompanyId(companyId)) {
            return;
        }
        accountRepository.saveAll(factory.createDefault(companyId));
    }

    @Transactional(readOnly = true)
    public AccountingAccount requireBySystemRole(UUID companyId, AccountingSystemRole systemRole) {
        return accountRepository
                .findByCompanyIdAndSystemRole(companyId, systemRole)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingAccount:" + systemRole.name(), companyId));
    }
}
