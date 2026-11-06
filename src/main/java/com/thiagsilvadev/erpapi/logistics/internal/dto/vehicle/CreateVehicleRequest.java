package com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.common.domain.model.PlateValid;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateVehicleRequest(
        @NotBlank @PlateValid
                @Schema(
                        description =
                                "Mercosul vehicle license plate (e.g. ABC1D23 or ABC-1D23), normalized to 7 uppercase alphanumeric characters",
                        example = "ABC1D23")
                String plate,
        @NotBlank String description,
        @Positive int capacity) {}
