package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryRouteRequest(
        @NotBlank(message = "Route name is required") String routeName,
        @NotNull(message = "Origin warehouse ID is required") UUID originWarehouseId,
        @NotBlank(message = "Destination description is required") String destinationDescription) {}
