package com.thiagsilvadev.erpapi.logistics.internal.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentStatus;

public record ShipmentResponse(
        UUID id,
        Long shipmentNumber,
        UUID salesOrderId,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        ShipmentStatus status,
        DriverResponse driver,
        VehicleResponse vehicle,
        String routeName,
        UUID originWarehouseId,
        String destinationDescription,
        Instant loadedAt,
        Instant dispatchedAt,
        Instant deliveredAt,
        Instant cancelledAt,
        List<ShipmentItemResponse> items,
        List<ShipmentTrackingEventResponse> trackingEvents) {}
