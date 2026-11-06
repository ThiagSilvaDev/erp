package com.thiagsilvadev.erpapi.financial.internal.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.BillingPartyType;
import com.thiagsilvadev.erpapi.financial.internal.model.InvoiceStatus;

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
