package com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment;

import java.time.Clock;
import java.time.Instant;

import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;

final class TrackingRequests {

    private TrackingRequests() {}

    static Instant occurredAt(CreateTrackingEventRequest request, Clock clock) {
        return request != null && request.occurredAt() != null ? request.occurredAt() : Instant.now(clock);
    }

    static String location(CreateTrackingEventRequest request) {
        return request == null ? null : request.location();
    }

    static String notes(CreateTrackingEventRequest request) {
        return request == null ? null : request.notes();
    }
}
