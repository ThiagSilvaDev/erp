package com.thiagsilvadev.erpapi.logistics.internal.dto;

import java.time.Instant;
import java.util.UUID;

import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentStatus;

public record ShipmentTrackingEventResponse(
        UUID id, ShipmentStatus status, Instant occurredAt, String location, String notes) {}
