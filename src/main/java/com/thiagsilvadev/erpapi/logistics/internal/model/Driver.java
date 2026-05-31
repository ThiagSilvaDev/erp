package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.util.Locale;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Phone;

@Entity
@Table(
        name = "logistics_drivers",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_logistics_driver_company_license_number",
                        columnNames = {"company_id", "license_number"}))
public class Driver extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "license_number", nullable = false, length = 60)
    private String licenseNumber;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone", nullable = false, length = 11))
    private Phone phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogisticsResourceStatus status = LogisticsResourceStatus.ACTIVE;

    protected Driver() {}

    private Driver(UUID companyId, String name, String licenseNumber, String phone) {
        this.companyId = java.util.Objects.requireNonNull(companyId, "Company ID is required");
        this.name = required(name, "Driver name cannot be blank");
        this.licenseNumber = normalize(licenseNumber, "Driver license number cannot be blank");
        this.phone = Phone.of(phone);
    }

    public static Driver create(UUID companyId, String name, String licenseNumber, String phone) {
        return new Driver(companyId, name, licenseNumber, phone);
    }

    public String getName() {
        return name;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getPhone() {
        return phone.getValue();
    }

    public LogisticsResourceStatus getStatus() {
        return status;
    }

    public static String normalizeLicenseNumber(String value) {
        return normalize(value, "Driver license number cannot be blank");
    }

    static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String normalize(String value, String message) {
        return required(value, message).toUpperCase(Locale.ROOT);
    }
}
