package com.thiagsilvadev.erpapi.logistics.internal.dto.driver;

import java.util.UUID;

import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;

public record DriverResponse(
        UUID id, String name, String licenseNumber, String phone, LogisticsResourceStatus status) {}
