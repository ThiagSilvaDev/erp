package com.thiagsilvadev.erpapi.financial.internal.dto.cashmovement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.CashMovementSource;
import com.thiagsilvadev.erpapi.financial.internal.model.CashMovementType;

public record CashMovementResponse(
        UUID id,
        UUID accountId,
        CashMovementType type,
        CashMovementSource source,
        BigDecimal amount,
        String currencyCode,
        UUID referenceId,
        String description,
        Instant occurredAt) {}
