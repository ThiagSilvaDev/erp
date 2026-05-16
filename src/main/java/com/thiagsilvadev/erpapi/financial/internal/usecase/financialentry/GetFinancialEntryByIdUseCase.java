package com.thiagsilvadev.erpapi.financial.internal.usecase.financialentry;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;

@Component
public class GetFinancialEntryByIdUseCase {

    private final FinancialEntryRepository entryRepository;
    private final FinancialMapper mapper;

    public GetFinancialEntryByIdUseCase(FinancialEntryRepository entryRepository, FinancialMapper mapper) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public FinancialEntryResponse execute(UUID id) {
        return mapper.toResponse(
                entryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(FinancialEntry.class, id)));
    }
}
