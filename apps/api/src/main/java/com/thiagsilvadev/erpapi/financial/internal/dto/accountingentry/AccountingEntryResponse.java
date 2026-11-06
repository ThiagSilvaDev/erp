package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntryDirection;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntrySourceType;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntryStatus;

public record AccountingEntryResponse(
        UUID id,
        UUID accountId,
        LocalDate competenceDate,
        BigDecimal amount,
        String currencyCode,
        AccountingEntryDirection direction,
        AccountingEntrySourceType sourceType,
        UUID sourceId,
        String description,
        AccountingEntryStatus status) {}
