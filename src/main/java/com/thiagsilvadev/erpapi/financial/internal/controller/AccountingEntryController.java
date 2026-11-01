package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceCriteria;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountingEntrySearchCriteria;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.CreateAccountingEntryRequest;
import com.thiagsilvadev.erpapi.financial.internal.usecase.accountingentry.CreateAccountingEntryUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.accountingentry.ListAccountBalancesUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.accountingentry.ListAccountingEntriesUseCase;

@RestController
class AccountingEntryController implements AccountingEntryApi {

    private final CreateAccountingEntryUseCase createAccountingEntryUseCase;
    private final ListAccountingEntriesUseCase listAccountingEntriesUseCase;
    private final ListAccountBalancesUseCase listAccountBalancesUseCase;

    AccountingEntryController(
            CreateAccountingEntryUseCase createAccountingEntryUseCase,
            ListAccountingEntriesUseCase listAccountingEntriesUseCase,
            ListAccountBalancesUseCase listAccountBalancesUseCase) {
        this.createAccountingEntryUseCase = createAccountingEntryUseCase;
        this.listAccountingEntriesUseCase = listAccountingEntriesUseCase;
        this.listAccountBalancesUseCase = listAccountBalancesUseCase;
    }

    @Override
    public ResponseEntity<AccountingEntryResponse> create(@Valid @RequestBody CreateAccountingEntryRequest request) {
        AccountingEntryResponse response = createAccountingEntryUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public Page<AccountingEntryResponse> list(AccountingEntrySearchCriteria criteria, Pageable pageable) {
        return listAccountingEntriesUseCase.execute(criteria, pageable);
    }

    @Override
    public List<AccountBalanceResponse> listBalances(@Valid AccountBalanceCriteria criteria) {
        return listAccountBalancesUseCase.execute(criteria);
    }
}
