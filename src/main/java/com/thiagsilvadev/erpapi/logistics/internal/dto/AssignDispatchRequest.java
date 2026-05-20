package com.thiagsilvadev.erpapi.logistics.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignDispatchRequest(
        @NotNull UUID driverId,
        @NotNull UUID vehicleId,
        @NotBlank String routeName,
        @NotNull UUID originWarehouseId,
        @NotBlank String destinationDescription) {}
