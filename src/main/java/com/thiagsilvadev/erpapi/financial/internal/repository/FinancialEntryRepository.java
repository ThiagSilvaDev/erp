package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;

public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, UUID> {

    Page<FinancialEntry> findByAccountId(UUID accountId, Pageable pageable);

    Page<FinancialEntry> findAllByCompanyId(UUID companyId, Pageable pageable);

    Page<FinancialEntry> findByCompanyIdAndAccountId(UUID companyId, UUID accountId, Pageable pageable);
}
