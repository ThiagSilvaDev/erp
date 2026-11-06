package com.thiagsilvadev.erpapi.financial.internal.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateInvoiceRequest(
        @NotNull BillingPartyType billingPartyType,
        @NotNull UUID billingPartyId,
        UUID salesOrderId,
        @NotNull @Positive BigDecimal amount,
        @NotNull @Schema(description = "Three-letter ISO 4217 currency code, normalized to uppercase", example = "BRL")
                String currencyCode,
        @NotNull LocalDate dueDate) {}
