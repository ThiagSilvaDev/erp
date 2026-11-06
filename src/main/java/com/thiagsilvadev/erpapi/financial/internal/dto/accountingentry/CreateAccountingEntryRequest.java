package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntryDirection;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateAccountingEntryRequest(
        @NotNull UUID accountId,
        @NotNull LocalDate competenceDate,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) @Schema(description = "Three-letter ISO 4217 currency code, normalized to uppercase", example = "BRL")
                String currencyCode,
        @NotNull AccountingEntryDirection direction,
        @Size(max = 255) String description) {}
