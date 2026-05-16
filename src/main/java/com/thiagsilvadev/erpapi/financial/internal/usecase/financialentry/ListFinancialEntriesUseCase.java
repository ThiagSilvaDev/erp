package com.thiagsilvadev.erpapi.financial.internal.usecase.financialentry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;

@Component
public class ListFinancialEntriesUseCase {

    private final FinancialEntryRepository entryRepository;
    private final FinancialMapper mapper;

    public ListFinancialEntriesUseCase(FinancialEntryRepository entryRepository, FinancialMapper mapper) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<FinancialEntryResponse> execute(Pageable pageable) {
        return entryRepository.findAll(pageable).map(mapper::toResponse);
    }
}
