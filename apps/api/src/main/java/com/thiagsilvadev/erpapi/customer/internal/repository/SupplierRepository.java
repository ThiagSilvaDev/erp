package com.thiagsilvadev.erpapi.customer.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, UUID>, JpaSpecificationExecutor<Supplier> {

    boolean existsByDocument(String document);

    boolean existsByCompanyIdAndDocument(UUID companyId, String document);

    Optional<Supplier> findByIdAndCompanyId(UUID id, UUID companyId);
}
