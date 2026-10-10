package com.thiagsilvadev.erpapi.logistics.internal.model;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;

public enum ShipmentStatus {
    CREATED,
    DISPATCH_ASSIGNED,
    LOADED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(ShipmentStatus target) {
        return switch (this) {
            case CREATED -> target == DISPATCH_ASSIGNED || target == CANCELLED;
            case DISPATCH_ASSIGNED -> target == DISPATCH_ASSIGNED || target == LOADED || target == CANCELLED;
            case LOADED -> target == IN_TRANSIT;
            case IN_TRANSIT -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public void requireTransitionTo(ShipmentStatus target) {
        if (!canTransitionTo(target)) {
            throw new ConflictException("Invalid shipment status transition from " + this + " to " + target);
        }
    }
}
