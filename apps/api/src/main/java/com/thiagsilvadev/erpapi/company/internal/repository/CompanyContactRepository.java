package com.thiagsilvadev.erpapi.company.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;

public interface CompanyContactRepository
        extends JpaRepository<CompanyContact, UUID>, JpaSpecificationExecutor<CompanyContact> {

    boolean existsByCompanyId(UUID companyId);

    Optional<CompanyContact> findByCompanyIdAndPrimaryContactTrue(UUID companyId);

    Optional<CompanyContact> findByIdAndCompanyId(UUID id, UUID companyId);

    List<CompanyContact> findByCompanyIdInAndPrimaryContactTrue(Collection<UUID> companyIds);

    Page<CompanyContact> findByCompanyId(UUID companyId, Pageable pageable);

    Page<CompanyContact> findByPrimaryContact(boolean primaryContact, Pageable pageable);

    Page<CompanyContact> findByCompanyIdAndPrimaryContact(UUID companyId, boolean primaryContact, Pageable pageable);
}
