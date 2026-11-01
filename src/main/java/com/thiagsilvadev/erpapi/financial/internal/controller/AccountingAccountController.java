package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount.AccountingAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount.CreateAccountingAccountRequest;
import com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount.AccountingAccountQueryUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount.CreateAccountingAccountUseCase;

@RestController
class AccountingAccountController implements AccountingAccountApi {

    private final CreateAccountingAccountUseCase createAccountingAccountUseCase;
    private final AccountingAccountQueryUseCase accountingAccountQueryUseCase;

    AccountingAccountController(
            CreateAccountingAccountUseCase createAccountingAccountUseCase,
            AccountingAccountQueryUseCase accountingAccountQueryUseCase) {
        this.createAccountingAccountUseCase = createAccountingAccountUseCase;
        this.accountingAccountQueryUseCase = accountingAccountQueryUseCase;
    }

    @Override
    public ResponseEntity<AccountingAccountResponse> createAccount(
            @Valid @RequestBody CreateAccountingAccountRequest request) {
        AccountingAccountResponse response = createAccountingAccountUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public AccountingAccountResponse getAccount(@PathVariable UUID id) {
        return accountingAccountQueryUseCase.getById(id);
    }

    @Override
    public Page<AccountingAccountResponse> listAccounts(Pageable pageable) {
        return accountingAccountQueryUseCase.list(pageable);
    }
}
