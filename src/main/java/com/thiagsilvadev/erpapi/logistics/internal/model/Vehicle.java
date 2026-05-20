package com.thiagsilvadev.erpapi.logistics.internal.model;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "logistics_vehicles",
        uniqueConstraints = @UniqueConstraint(name = "uk_logistics_vehicle_plate", columnNames = "plate"))
public class Vehicle extends AbstractBaseEntity {

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

    private Vehicle(String plate, String description, int capacity) {
        this.plate = normalizePlate(plate);
        this.description = Driver.required(description, "Vehicle description cannot be blank");
        if (capacity <= 0) {
            throw new IllegalArgumentException("Vehicle capacity must be positive");
        }
        this.capacity = capacity;
    }

    public static Vehicle create(String plate, String description, int capacity) {
        return new Vehicle(plate, description, capacity);
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
