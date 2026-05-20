package com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.*;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;

@Component
public class DeliverShipmentUseCase {

    private final ShipmentReader shipmentReader;
    private final ShipmentTrackingEventRepository trackingEventRepository;
    private final Clock clock;

    public DeliverShipmentUseCase(
            ShipmentReader shipmentReader, ShipmentTrackingEventRepository trackingEventRepository, Clock clock) {
        this.shipmentReader = shipmentReader;
        this.trackingEventRepository = trackingEventRepository;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions).DISPATCH)")
    public ShipmentResponse execute(UUID shipmentId, CreateTrackingEventRequest request) {
        Shipment shipment = shipmentReader.get(shipmentId);
        Instant occurredAt = TrackingRequests.occurredAt(request, clock);
        shipment.deliver(occurredAt);
        trackingEventRepository.save(ShipmentTrackingEvent.create(
                shipment,
                ShipmentStatus.DELIVERED,
                occurredAt,
                TrackingRequests.location(request),
                TrackingRequests.notes(request)));
        return shipmentReader.toResponse(shipment);
    }
}
