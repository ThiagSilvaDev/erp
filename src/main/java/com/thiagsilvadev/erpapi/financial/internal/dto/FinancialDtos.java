package com.thiagsilvadev.erpapi.financial.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
            UUID destinationAccountId,
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
            UUID destinationAccountId,
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

    public record CreateFinancialAccountRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull FinancialAccountType type,
            @Size(max = 120) String description) {}

    public record FinancialAccountResponse(
            UUID id, String name, FinancialAccountType type, String description, FinancialResourceStatus status) {}

    public record FinancialEntryResponse(
            UUID id,
            UUID accountId,
            FinancialEntryType type,
            FinancialEntrySource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description,
            Instant occurredAt) {}
}
