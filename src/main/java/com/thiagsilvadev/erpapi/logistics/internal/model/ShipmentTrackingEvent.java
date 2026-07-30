package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(name = "logistics_shipment_tracking_events")
public class ShipmentTrackingEvent extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column
    private String location;

    @Column
    private String notes;

    @Column(name = "performed_by_user_id")
    private UUID performedByUserId;

    protected ShipmentTrackingEvent() {}

    private ShipmentTrackingEvent(
            Shipment shipment,
            ShipmentStatus status,
            Instant occurredAt,
            String location,
            String notes,
            UUID performedByUserId) {
        this.shipment = Objects.requireNonNull(shipment, "Shipment is required");
        this.status = Objects.requireNonNull(status, "Tracking status is required");
        this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred timestamp is required");
        this.location = trimToNull(location);
        this.notes = trimToNull(notes);
        this.performedByUserId = performedByUserId;
    }

    public static ShipmentTrackingEvent create(
            Shipment shipment, ShipmentStatus status, Instant occurredAt, String location, String notes) {
        return new ShipmentTrackingEvent(shipment, status, occurredAt, location, notes, null);
    }

    public static ShipmentTrackingEvent createByUser(
            Shipment shipment,
            ShipmentStatus status,
            Instant occurredAt,
            String location,
            String notes,
            UUID performedByUserId) {
        return new ShipmentTrackingEvent(
                shipment,
                status,
                occurredAt,
                location,
                notes,
                Objects.requireNonNull(performedByUserId, "Performed-by user is required"));
    }

    public Shipment getShipment() {
        return shipment;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getLocation() {
        return location;
    }

    public String getNotes() {
        return notes;
    }

    public UUID getPerformedByUserId() {
        return performedByUserId;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
