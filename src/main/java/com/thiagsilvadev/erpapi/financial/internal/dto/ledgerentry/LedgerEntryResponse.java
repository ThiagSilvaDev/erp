package com.thiagsilvadev.erpapi.financial.internal.dto.ledgerentry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntryStatus;
import com.thiagsilvadev.erpapi.financial.internal.model.LedgerEntryType;

public record LedgerEntryResponse(
        UUID id,
        LedgerEntryType type,
        UUID invoiceId,
        BigDecimal amount,
        String currencyCode,
        LocalDate dueDate,
        LedgerEntryStatus status) {}
