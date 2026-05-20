package com.thiagsilvadev.erpapi.financial.internal.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.PaymentStatus;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;

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
