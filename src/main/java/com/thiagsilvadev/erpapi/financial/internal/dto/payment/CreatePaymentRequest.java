package com.thiagsilvadev.erpapi.financial.internal.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.financial.internal.model.PaymentStatus;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreatePaymentRequest(
        @NotNull UUID salesOrderId,
        @NotNull UUID customerId,
        UUID invoiceId,
        @Schema(description = "Financial account receiving/disbursing funds; required when status is PAID")
                UUID destinationAccountId,
        @NotNull @Positive BigDecimal amount,
        @NotNull PaymentMethod method,
        @NotNull PaymentStatus status,
        LocalDate dueDate,
        LocalDate paidAt) {}
