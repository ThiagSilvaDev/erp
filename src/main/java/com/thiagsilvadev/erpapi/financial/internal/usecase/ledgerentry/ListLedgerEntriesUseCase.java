package com.thiagsilvadev.erpapi.financial.internal.usecase.ledgerentry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.LedgerEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.repository.LedgerEntryRepository;

@Component
public class ListLedgerEntriesUseCase {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final FinancialMapper mapper;

    public ListLedgerEntriesUseCase(LedgerEntryRepository ledgerEntryRepository, FinancialMapper mapper) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> execute(Pageable pageable) {
        return ledgerEntryRepository.findAll(pageable).map(mapper::toResponse);
    }
}
