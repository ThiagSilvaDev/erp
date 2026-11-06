package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialObligation;

public interface FinancialObligationRepository extends JpaRepository<FinancialObligation, UUID> {

    Optional<FinancialObligation> findByInvoiceId(UUID invoiceId);

    Optional<FinancialObligation> findByCompanyIdAndInvoiceId(UUID companyId, UUID invoiceId);

    Optional<FinancialObligation> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<FinancialObligation> findAllByCompanyId(UUID companyId, Pageable pageable);
}
