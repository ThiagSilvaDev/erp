package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.usecase.financialentry.GetFinancialEntryByIdUseCase;
import com.thiagsilvadev.erpapi.financial.internal.usecase.financialentry.ListFinancialEntriesUseCase;

@RestController
class FinancialEntryController implements FinancialEntryApi {

    private final GetFinancialEntryByIdUseCase getFinancialEntryByIdUseCase;
    private final ListFinancialEntriesUseCase listFinancialEntriesUseCase;

    FinancialEntryController(
            GetFinancialEntryByIdUseCase getFinancialEntryByIdUseCase,
            ListFinancialEntriesUseCase listFinancialEntriesUseCase) {
        this.getFinancialEntryByIdUseCase = getFinancialEntryByIdUseCase;
        this.listFinancialEntriesUseCase = listFinancialEntriesUseCase;
    }

    @Override
    public ResponseEntity<FinancialEntryResponse> getEntry(@PathVariable UUID id) {
        return ResponseEntity.ok(getFinancialEntryByIdUseCase.execute(id));
    }

    @Override
    public ResponseEntity<Page<FinancialEntryResponse>> listEntries(Pageable pageable) {
        return ResponseEntity.ok(listFinancialEntriesUseCase.execute(pageable));
    }
}
