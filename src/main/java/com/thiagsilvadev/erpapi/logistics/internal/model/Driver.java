package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(
        name = "logistics_drivers",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_logistics_driver_company_license_number",
                    columnNames = {"company_id", "license_number"}),
            @UniqueConstraint(
                    name = "uk_logistics_driver_company_user",
                    columnNames = {"company_id", "user_id"})
        })
public class Driver extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "license_number", nullable = false, length = 60)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogisticsResourceStatus status = LogisticsResourceStatus.ACTIVE;

    protected Driver() {}

    private Driver(UUID companyId, UUID userId, String licenseNumber) {
        this.companyId = Objects.requireNonNull(companyId, "Company ID is required");
        this.userId = Objects.requireNonNull(userId, "User ID is required");
        this.licenseNumber = normalize(licenseNumber, "Driver license number cannot be blank");
    }

    public static Driver create(UUID companyId, UUID userId, String licenseNumber) {
        return new Driver(companyId, userId, licenseNumber);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public LogisticsResourceStatus getStatus() {
        return status;
    }

    public static String normalizeLicenseNumber(String value) {
        return normalize(value, "Driver license number cannot be blank");
    }

    private static String normalize(String value, String message) {
        return Strings.required(value, message).toUpperCase(Locale.ROOT);
    }
}
