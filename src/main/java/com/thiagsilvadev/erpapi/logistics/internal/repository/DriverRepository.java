package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    boolean existsByLicenseNumber(String licenseNumber);
}
