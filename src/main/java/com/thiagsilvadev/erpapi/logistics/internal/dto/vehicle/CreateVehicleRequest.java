package com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.common.domain.model.PlateValid;

public record CreateVehicleRequest(
        @NotBlank @PlateValid String plate, @NotBlank String description, @Positive int capacity) {}
