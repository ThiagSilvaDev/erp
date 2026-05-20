package com.thiagsilvadev.erpapi.logistics.internal.dto.shipment;

import java.time.Instant;

public record CreateTrackingEventRequest(Instant occurredAt, String location, String notes) {}
