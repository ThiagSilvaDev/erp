package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.CreateFinancialAccountRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.usecase.account.CreateFinancialAccountUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.account.GetFinancialAccountByIdUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.account.ListFinancialAccountsUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.entry.ListFinancialEntriesUseCase;

@RestController
class FinancialAccountController implements FinancialAccountApi {

    private final CreateFinancialAccountUseCase createFinancialAccountUseCase;
    private final GetFinancialAccountByIdUseCase getFinancialAccountByIdUseCase;
    private final ListFinancialAccountsUseCase listFinancialAccountsUseCase;
    private final ListFinancialEntriesUseCase listFinancialEntriesUseCase;

    FinancialAccountController(
            CreateFinancialAccountUseCase createFinancialAccountUseCase,
            GetFinancialAccountByIdUseCase getFinancialAccountByIdUseCase,
            ListFinancialAccountsUseCase listFinancialAccountsUseCase,
            ListFinancialEntriesUseCase listFinancialEntriesUseCase) {
        this.createFinancialAccountUseCase = createFinancialAccountUseCase;
        this.getFinancialAccountByIdUseCase = getFinancialAccountByIdUseCase;
        this.listFinancialAccountsUseCase = listFinancialAccountsUseCase;
        this.listFinancialEntriesUseCase = listFinancialEntriesUseCase;
    }

    @Override
    public ResponseEntity<FinancialAccountResponse> createAccount(
            @Valid @RequestBody CreateFinancialAccountRequest request) {
        FinancialAccountResponse response = createFinancialAccountUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public FinancialAccountResponse getAccount(@PathVariable UUID id) {
        return getFinancialAccountByIdUseCase.execute(id);
    }

    @Override
    public Page<FinancialAccountResponse> listAccounts(Pageable pageable) {
        return listFinancialAccountsUseCase.execute(pageable);
    }

    @Override
    public Page<FinancialEntryResponse> listEntries(@PathVariable UUID accountId, Pageable pageable) {
        return listFinancialEntriesUseCase.execute(accountId, pageable);
    }
}
