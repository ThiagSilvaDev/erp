package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByPlate(String plate);
}
