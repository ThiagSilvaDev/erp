package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.util.Locale;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Phone;

@Entity
@Table(
        name = "logistics_drivers",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_logistics_driver_license_number", columnNames = "license_number"))
public class Driver extends AbstractBaseEntity {

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

    private Driver(String name, String licenseNumber, String phone) {
        this.name = required(name, "Driver name cannot be blank");
        this.licenseNumber = normalize(licenseNumber, "Driver license number cannot be blank");
        this.phone = Phone.of(phone);
    }

    public static Driver create(String name, String licenseNumber, String phone) {
        return new Driver(name, licenseNumber, phone);
    }

    public String getName() {
        return name;
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
