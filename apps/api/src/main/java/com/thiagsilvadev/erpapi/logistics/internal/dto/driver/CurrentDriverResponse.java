package com.thiagsilvadev.erpapi.logistics.internal.dto.driver;

import java.util.UUID;

import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;

public record CurrentDriverResponse(
        UUID id,
        UUID userId,
        String displayName,
        String email,
        String phone,
        String licenseNumber,
        LogisticsResourceStatus status) {}
