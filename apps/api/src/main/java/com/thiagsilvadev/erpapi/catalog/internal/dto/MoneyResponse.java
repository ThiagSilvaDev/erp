package com.thiagsilvadev.erpapi.catalog.internal.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record MoneyResponse(
        @Schema(description = "Non-negative monetary amount with at most 2 decimal places", example = "99.90")
                BigDecimal amount,
        @Schema(description = "Three-letter ISO 4217 currency code, normalized to uppercase", example = "BRL")
                String currencyCode) {}
