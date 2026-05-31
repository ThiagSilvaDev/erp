package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "logistics_vehicles",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_logistics_vehicle_company_plate",
                        columnNames = {"company_id", "plate"}))
public class Vehicle extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(nullable = false, length = 20)
    private String plate;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogisticsResourceStatus status = LogisticsResourceStatus.ACTIVE;

    protected Vehicle() {}

    private Vehicle(UUID companyId, String plate, String description, int capacity) {
        this.companyId = java.util.Objects.requireNonNull(companyId, "Company ID is required");
        this.plate = normalizePlate(plate);
        this.description = Driver.required(description, "Vehicle description cannot be blank");
        if (capacity <= 0) {
            throw new IllegalArgumentException("Vehicle capacity must be positive");
        }
        this.capacity = capacity;
    }

    public static Vehicle create(UUID companyId, String plate, String description, int capacity) {
        return new Vehicle(companyId, plate, description, capacity);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getPlate() {
        return plate;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public LogisticsResourceStatus getStatus() {
        return status;
    }

    public static String normalizePlate(String plate) {
        return Driver.required(plate, "Vehicle plate cannot be blank").toUpperCase();
    }
}
