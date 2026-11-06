package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public record GeoLocationRequest(
        @NotNull(message = "Latitude is required") @Schema(
                        description = "Latitude in decimal degrees between -90 and 90 (up to 7 decimal places)",
                        example = "-23.5505199")
                BigDecimal latitude,
        @NotNull(message = "Longitude is required") @Schema(
                        description = "Longitude in decimal degrees between -180 and 180 (up to 7 decimal places)",
                        example = "-46.6333094")
                BigDecimal longitude) {}
