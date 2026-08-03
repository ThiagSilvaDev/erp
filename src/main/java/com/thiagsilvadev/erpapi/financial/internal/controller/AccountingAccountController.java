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
import com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount.CreateAccountingAccountUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount.GetAccountingAccountByIdUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount.ListAccountingAccountsUseCase;

@RestController
class AccountingAccountController implements AccountingAccountApi {

    private final CreateAccountingAccountUseCase createAccountingAccountUseCase;
    private final GetAccountingAccountByIdUseCase getAccountingAccountByIdUseCase;
    private final ListAccountingAccountsUseCase listAccountingAccountsUseCase;

    AccountingAccountController(
            CreateAccountingAccountUseCase createAccountingAccountUseCase,
            GetAccountingAccountByIdUseCase getAccountingAccountByIdUseCase,
            ListAccountingAccountsUseCase listAccountingAccountsUseCase) {
        this.createAccountingAccountUseCase = createAccountingAccountUseCase;
        this.getAccountingAccountByIdUseCase = getAccountingAccountByIdUseCase;
        this.listAccountingAccountsUseCase = listAccountingAccountsUseCase;
    }

    @Override
    public ResponseEntity<AccountingAccountResponse> createAccount(
            @Valid @RequestBody CreateAccountingAccountRequest request) {
        AccountingAccountResponse response = createAccountingAccountUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public AccountingAccountResponse getAccount(@PathVariable UUID id) {
        return getAccountingAccountByIdUseCase.execute(id);
    }

    @Override
    public Page<AccountingAccountResponse> listAccounts(Pageable pageable) {
        return listAccountingAccountsUseCase.execute(pageable);
    }
}
