package com.thiagsilvadev.erpapi.financial.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.financial.internal.model.*;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;

public final class FinancialDtos {

    private FinancialDtos() {}

    public record CreateInvoiceRequest(
            @NotNull BillingPartyType billingPartyType,
            @NotNull UUID billingPartyId,
            UUID salesOrderId,
            @NotNull @Positive BigDecimal amount,
            @NotNull String currencyCode,
            @NotNull LocalDate dueDate) {}

    public record InvoiceResponse(
            UUID id,
            Long invoiceNumber,
            BillingPartyType billingPartyType,
            UUID billingPartyId,
            UUID salesOrderId,
            BigDecimal amount,
            String currencyCode,
            LocalDate dueDate,
            InvoiceStatus status) {}

    public record InvoiceSearchCriteria(UUID salesOrderId) {}

    public record CreatePaymentRequest(
            @NotNull UUID salesOrderId,
            @NotNull UUID customerId,
            UUID invoiceId,
            @NotNull @Positive BigDecimal amount,
            @NotNull PaymentMethod method,
            @NotNull PaymentStatus status,
            LocalDate dueDate,
            LocalDate paidAt) {}

    public record PaymentResponse(
            UUID id,
            Long paymentNumber,
            UUID salesOrderId,
            UUID customerId,
            UUID invoiceId,
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status,
            LocalDate dueDate,
            LocalDate paidAt) {}

    public record LedgerEntryResponse(
            UUID id,
            LedgerEntryType type,
            UUID invoiceId,
            BigDecimal amount,
            String currencyCode,
            LocalDate dueDate,
            LedgerEntryStatus status) {}
}
