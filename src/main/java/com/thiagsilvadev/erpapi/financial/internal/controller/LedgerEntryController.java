package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.ledgerentry.LedgerEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry.LedgerEntryQueryUseCase;

@RestController
class LedgerEntryController implements LedgerEntryApi {

    private final LedgerEntryQueryUseCase ledgerEntryQueryUseCase;

    LedgerEntryController(LedgerEntryQueryUseCase ledgerEntryQueryUseCase) {
        this.ledgerEntryQueryUseCase = ledgerEntryQueryUseCase;
    }

    @Override
    public LedgerEntryResponse getLedgerEntry(@PathVariable UUID id) {
        return ledgerEntryQueryUseCase.getById(id);
    }

    @Override
    public Page<LedgerEntryResponse> listLedgerEntries(Pageable pageable) {
        return ledgerEntryQueryUseCase.list(pageable);
    }
}
