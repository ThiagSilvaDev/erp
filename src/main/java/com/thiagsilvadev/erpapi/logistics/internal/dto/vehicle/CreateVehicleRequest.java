package com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateVehicleRequest(@NotBlank String plate, @NotBlank String description, @Positive int capacity) {}
