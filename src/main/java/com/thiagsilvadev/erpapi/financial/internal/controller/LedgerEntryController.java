package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.ledgerentry.LedgerEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry.GetLedgerEntryByIdUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry.ListLedgerEntriesUseCase;

@RestController
class LedgerEntryController implements LedgerEntryApi {

    private final GetLedgerEntryByIdUseCase getLedgerEntryByIdUseCase;
    private final ListLedgerEntriesUseCase listLedgerEntriesUseCase;

    LedgerEntryController(
            GetLedgerEntryByIdUseCase getLedgerEntryByIdUseCase, ListLedgerEntriesUseCase listLedgerEntriesUseCase) {
        this.getLedgerEntryByIdUseCase = getLedgerEntryByIdUseCase;
        this.listLedgerEntriesUseCase = listLedgerEntriesUseCase;
    }

    @Override
    public LedgerEntryResponse getLedgerEntry(@PathVariable UUID id) {
        return getLedgerEntryByIdUseCase.execute(id);
    }

    @Override
    public Page<LedgerEntryResponse> listLedgerEntries(Pageable pageable) {
        return listLedgerEntriesUseCase.execute(pageable);
    }
}
