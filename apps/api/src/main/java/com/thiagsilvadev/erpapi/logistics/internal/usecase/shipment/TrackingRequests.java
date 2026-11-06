package com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment;

import java.time.Clock;
import java.time.Instant;

import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;

public final class TrackingRequests {

    private TrackingRequests() {}

    public static Instant occurredAt(CreateTrackingEventRequest request, Clock clock) {
        return request != null && request.occurredAt() != null ? request.occurredAt() : Instant.now(clock);
    }

    public static String location(CreateTrackingEventRequest request) {
        return request == null ? null : request.location();
    }

    public static String notes(CreateTrackingEventRequest request) {
        return request == null ? null : request.notes();
    }
}
