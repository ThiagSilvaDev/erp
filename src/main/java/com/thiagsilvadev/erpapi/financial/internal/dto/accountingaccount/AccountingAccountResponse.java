package com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount;

import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccountType;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingSystemRole;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialResourceStatus;

public record AccountingAccountResponse(
        UUID id,
        String code,
        String name,
        String description,
        UUID parentId,
        AccountingAccountType type,
        AccountingSystemRole systemRole,
        boolean acceptsPostings,
        FinancialResourceStatus status) {}
