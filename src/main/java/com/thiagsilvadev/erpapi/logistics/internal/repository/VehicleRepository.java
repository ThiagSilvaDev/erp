package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByPlate(String plate);

    boolean existsByCompanyIdAndPlate(UUID companyId, String plate);

    Optional<Vehicle> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<Vehicle> findAllByCompanyId(UUID companyId, Pageable pageable);
}
