package com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.*;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;

@Component
public class LoadShipmentUseCase {

    private final ShipmentReader shipmentReader;
    private final ShipmentTrackingEventRepository trackingEventRepository;
    private final InventoryFacade inventoryFacade;
    private final Clock clock;

    public LoadShipmentUseCase(
            ShipmentReader shipmentReader,
            ShipmentTrackingEventRepository trackingEventRepository,
            InventoryFacade inventoryFacade,
            Clock clock) {
        this.shipmentReader = shipmentReader;
        this.trackingEventRepository = trackingEventRepository;
        this.inventoryFacade = inventoryFacade;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions).DISPATCH)")
    public ShipmentResponse execute(UUID shipmentId, CreateTrackingEventRequest request) {
        Shipment shipment = shipmentReader.get(shipmentId);
        Instant occurredAt = TrackingRequests.occurredAt(request, clock);
        inventoryFacade.consumeReservationsForSalesOrder(shipment.getSalesOrderId());
        shipment.load(occurredAt);
        trackingEventRepository.save(ShipmentTrackingEvent.create(
                shipment,
                ShipmentStatus.LOADED,
                occurredAt,
                TrackingRequests.location(request),
                TrackingRequests.notes(request)));
        return shipmentReader.toResponse(shipment);
    }
}
