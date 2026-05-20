package com.thiagsilvadev.erpapi.logistics.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;

public record VehicleResponse(
        UUID id, String plate, String description, int capacity, LogisticsResourceStatus status) {}
