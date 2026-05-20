package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record GeoLocationRequest(
        @NotNull(message = "Latitude is required") BigDecimal latitude,
        @NotNull(message = "Longitude is required") BigDecimal longitude) {}
