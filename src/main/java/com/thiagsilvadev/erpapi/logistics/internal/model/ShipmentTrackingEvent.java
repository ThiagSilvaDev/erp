package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.time.Instant;

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

    protected ShipmentTrackingEvent() {}

    private ShipmentTrackingEvent(
            Shipment shipment, ShipmentStatus status, Instant occurredAt, String location, String notes) {
        this.shipment = java.util.Objects.requireNonNull(shipment, "Shipment is required");
        this.status = java.util.Objects.requireNonNull(status, "Tracking status is required");
        this.occurredAt = java.util.Objects.requireNonNull(occurredAt, "Occurred timestamp is required");
        this.location = trimToNull(location);
        this.notes = trimToNull(notes);
    }

    public static ShipmentTrackingEvent create(
            Shipment shipment, ShipmentStatus status, Instant occurredAt, String location, String notes) {
        return new ShipmentTrackingEvent(shipment, status, occurredAt, location, notes);
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

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
