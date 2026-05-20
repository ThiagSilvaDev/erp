package com.thiagsilvadev.erpapi.financial.internal.dto.entry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntrySource;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntryType;

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
