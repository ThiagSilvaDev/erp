package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.DRIVER_WORKFLOW;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentStatus;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentTrackingEvent;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.ShipmentReader;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.TrackingRequests;

@Component
public class CurrentDriverShipmentWorkflowUseCase {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingEventRepository trackingEventRepository;
    private final ShipmentReader shipmentReader;
    private final InventoryFacade inventoryFacade;
    private final Clock clock;

    public CurrentDriverShipmentWorkflowUseCase(
            ShipmentRepository shipmentRepository,
            ShipmentTrackingEventRepository trackingEventRepository,
            ShipmentReader shipmentReader,
            InventoryFacade inventoryFacade,
            Clock clock) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.shipmentReader = shipmentReader;
        this.inventoryFacade = inventoryFacade;
        this.clock = clock;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + DRIVER_WORKFLOW + "')")
    public ShipmentResponse load(UUID userId, UUID shipmentId, CreateTrackingEventRequest request) {
        Shipment shipment = assignedShipment(userId, shipmentId);
        Instant occurredAt = TrackingRequests.occurredAt(request, clock);
        inventoryFacade.consumeReservationsForSalesOrder(shipment.getSellerCompanyId(), shipment.getSalesOrderId());
        shipment.load(occurredAt);
        saveEvent(shipment, ShipmentStatus.LOADED, occurredAt, request, userId);
        return shipmentReader.toResponse(shipment);
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + DRIVER_WORKFLOW + "')")
    public ShipmentResponse dispatch(UUID userId, UUID shipmentId, CreateTrackingEventRequest request) {
        Shipment shipment = assignedShipment(userId, shipmentId);
        Instant occurredAt = TrackingRequests.occurredAt(request, clock);
        shipment.dispatch(occurredAt);
        saveEvent(shipment, ShipmentStatus.IN_TRANSIT, occurredAt, request, userId);
        return shipmentReader.toResponse(shipment);
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + DRIVER_WORKFLOW + "')")
    public ShipmentResponse track(UUID userId, UUID shipmentId, CreateTrackingEventRequest request) {
        Shipment shipment = assignedShipment(userId, shipmentId);
        Instant occurredAt = TrackingRequests.occurredAt(request, clock);
        saveEvent(shipment, shipment.getStatus(), occurredAt, request, userId);
        return shipmentReader.toResponse(shipment);
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + DRIVER_WORKFLOW + "')")
    public ShipmentResponse deliver(UUID userId, UUID shipmentId, CreateTrackingEventRequest request) {
        Shipment shipment = assignedShipment(userId, shipmentId);
        Instant occurredAt = TrackingRequests.occurredAt(request, clock);
        shipment.deliver(occurredAt);
        saveEvent(shipment, ShipmentStatus.DELIVERED, occurredAt, request, userId);
        return shipmentReader.toResponse(shipment);
    }

    private Shipment assignedShipment(UUID userId, UUID shipmentId) {
        UUID companyId = TenantContext.requireTenantId();
        return shipmentRepository
                .findByIdAndSellerCompanyIdAndDriverUserIdAndDriverStatus(
                        shipmentId, companyId, userId, LogisticsResourceStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(Shipment.class, shipmentId));
    }

    private void saveEvent(
            Shipment shipment,
            ShipmentStatus status,
            Instant occurredAt,
            CreateTrackingEventRequest request,
            UUID userId) {
        trackingEventRepository.save(ShipmentTrackingEvent.createByUser(
                shipment,
                status,
                occurredAt,
                TrackingRequests.location(request),
                TrackingRequests.notes(request),
                userId));
    }
}
