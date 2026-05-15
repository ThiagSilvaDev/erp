package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;

public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, UUID> {

    Optional<FinancialEntry> findByInvoiceId(UUID invoiceId);
}
