package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccount;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCompanyIdAndNameIgnoreCase(UUID companyId, String name);

    Optional<FinancialAccount> findByName(String name);

    Optional<FinancialAccount> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<FinancialAccount> findAllByCompanyId(UUID companyId, Pageable pageable);
}
