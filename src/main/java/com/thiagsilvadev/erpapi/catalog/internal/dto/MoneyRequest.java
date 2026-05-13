package com.thiagsilvadev.erpapi.catalog.internal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MoneyRequest(@NotNull @PositiveOrZero BigDecimal amount, @NotBlank String currencyCode) {}
