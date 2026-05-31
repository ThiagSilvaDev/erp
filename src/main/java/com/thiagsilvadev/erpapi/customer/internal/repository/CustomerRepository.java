package com.thiagsilvadev.erpapi.customer.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.customer.internal.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    boolean existsByDocument(String document);

    boolean existsByOwnerCompanyIdAndDocument(UUID ownerCompanyId, String document);

    Optional<Customer> findByDocument(String document);

    Optional<Customer> findByIdAndOwnerCompanyId(UUID id, UUID ownerCompanyId);

    Optional<Customer> findByOwnerCompanyIdAndDocument(UUID ownerCompanyId, String document);
}
