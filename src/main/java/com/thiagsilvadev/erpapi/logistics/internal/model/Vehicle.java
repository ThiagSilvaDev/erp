package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Plate;
import com.thiagsilvadev.erpapi.common.domain.model.PlateType;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

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

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "plate", nullable = false, length = 10))
    @AttributeOverride(name = "type", column = @Column(name = "plate_type", nullable = false, length = 20))
    private Plate plate;

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
        this.plate = Plate.of(plate, PlateType.MERCOSUL);
        this.description = Strings.required(description, "Vehicle description cannot be blank");
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
        return plate.getValue();
    }

    public Plate getLicensePlate() {
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
}
