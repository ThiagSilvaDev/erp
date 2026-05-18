package com.thiagsilvadev.erpapi.inventory.internal.dto.reservation;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReservationRequest(
        @NotNull UUID productId, @NotNull UUID warehouseId, @Positive int quantity, @NotNull UUID salesOrderId) {}
