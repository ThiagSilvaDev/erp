package com.thiagsilvadev.erpapi.financial.internal.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialDtos.FinancialEntryResponse;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialEntryRepository;

@RestController
class FinancialEntryController implements FinancialEntryApi {

    private final FinancialEntryRepository entryRepository;
    private final FinancialMapper mapper;

    FinancialEntryController(FinancialEntryRepository entryRepository, FinancialMapper mapper) {
        this.entryRepository = entryRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<FinancialEntryResponse> getEntry(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(entryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FinancialEntry.class, id))));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Page<FinancialEntryResponse>> listEntries(Pageable pageable) {
        return ResponseEntity.ok(entryRepository.findAll(pageable).map(mapper::toResponse));
    }
}
