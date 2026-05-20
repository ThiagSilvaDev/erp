package com.thiagsilvadev.erpapi.logistics.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentStatus;

public record ShipmentSearchCriteria(UUID salesOrderId, UUID driverId, UUID vehicleId, ShipmentStatus status) {}
