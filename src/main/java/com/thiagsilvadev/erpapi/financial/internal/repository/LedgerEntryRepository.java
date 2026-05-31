package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    Optional<LedgerEntry> findByInvoiceId(UUID invoiceId);

    Optional<LedgerEntry> findByCompanyIdAndInvoiceId(UUID companyId, UUID invoiceId);

    Optional<LedgerEntry> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<LedgerEntry> findAllByCompanyId(UUID companyId, Pageable pageable);
}
