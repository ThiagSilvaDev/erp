package com.thiagsilvadev.erpapi.financial.internal.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;

public record CreateInvoiceRequest(
        @NotNull BillingPartyType billingPartyType,
        @NotNull UUID billingPartyId,
        UUID salesOrderId,
        @NotNull @Positive BigDecimal amount,
        @NotNull String currencyCode,
        @NotNull LocalDate dueDate) {}
