package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByCompanyIdAndLicenseNumber(UUID companyId, String licenseNumber);

    Optional<Driver> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<Driver> findAllByCompanyId(UUID companyId, Pageable pageable);
}
