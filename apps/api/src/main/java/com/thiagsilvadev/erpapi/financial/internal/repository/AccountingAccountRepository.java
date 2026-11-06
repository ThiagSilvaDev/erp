package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccount;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingSystemRole;

public interface AccountingAccountRepository extends JpaRepository<AccountingAccount, UUID> {

    Optional<AccountingAccount> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<AccountingAccount> findAllByCompanyId(UUID companyId, Pageable pageable);

    List<AccountingAccount> findAllByCompanyIdOrderByCodeAsc(UUID companyId);

    boolean existsByCompanyIdAndCodeIgnoreCase(UUID companyId, String code);

    boolean existsByCompanyId(UUID companyId);

    Optional<AccountingAccount> findByCompanyIdAndSystemRole(UUID companyId, AccountingSystemRole systemRole);

    boolean existsByCompanyIdAndSystemRole(UUID companyId, AccountingSystemRole systemRole);
}
