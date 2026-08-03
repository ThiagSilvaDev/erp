package com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry;

import java.math.BigDecimal;
import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccountType;

public record AccountBalanceResponse(
        UUID accountId,
        String accountCode,
        String accountName,
        AccountingAccountType accountType,
        String currencyCode,
        BigDecimal amount) {}
