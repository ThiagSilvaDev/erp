package com.thiagsilvadev.erpapi.financial.internal.dto.financialobligation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialObligationStatus;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialObligationType;

public record FinancialObligationResponse(
        UUID id,
        FinancialObligationType type,
        UUID invoiceId,
        BigDecimal amount,
        String currencyCode,
        LocalDate dueDate,
        FinancialObligationStatus status) {}
