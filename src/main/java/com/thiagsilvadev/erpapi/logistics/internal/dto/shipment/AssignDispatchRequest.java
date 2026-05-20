package com.thiagsilvadev.erpapi.logistics.internal.dto.shipment;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignDispatchRequest(
        @NotNull UUID driverId,
        @NotNull UUID vehicleId,
        String routeName,
        UUID originWarehouseId,
        String destinationDescription) {}
