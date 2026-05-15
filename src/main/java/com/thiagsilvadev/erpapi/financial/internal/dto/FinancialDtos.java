package com.thiagsilvadev.erpapi.financial.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.financial.internal.model.*;

public final class FinancialDtos {

    private FinancialDtos() {}

    public record CreateInvoiceRequest(
            @NotNull UUID customerId,
            UUID salesOrderId,
            @NotNull @Positive BigDecimal amount,
            @NotNull String currencyCode,
            @NotNull LocalDate dueDate) {}

    public record InvoiceResponse(
            UUID id,
            UUID customerId,
            UUID salesOrderId,
            BigDecimal amount,
            String currencyCode,
            LocalDate dueDate,
            InvoiceStatus status) {}

    public record CreatePaymentRequest(
            @NotNull UUID invoiceId, @NotNull @Positive BigDecimal amount, @NotNull LocalDate paidAt) {}

    public record PaymentResponse(UUID id, UUID invoiceId, BigDecimal amount, LocalDate paidAt) {}

    public record FinancialEntryResponse(
            UUID id,
            EntryType type,
            UUID invoiceId,
            BigDecimal amount,
            String currencyCode,
            LocalDate dueDate,
            EntryStatus status) {}
}
