package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccount;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Optional<FinancialAccount> findByName(String name);
}
