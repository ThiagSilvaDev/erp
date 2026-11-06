package com.thiagsilvadev.erpapi.catalog.internal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import io.swagger.v3.oas.annotations.media.Schema;

public record MoneyRequest(
        @NotNull @PositiveOrZero @Schema(description = "Non-negative monetary amount with at most 2 decimal places", example = "99.90")
                BigDecimal amount,
        @NotBlank @Schema(description = "Three-letter ISO 4217 currency code, normalized to uppercase", example = "BRL")
                String currencyCode) {}
