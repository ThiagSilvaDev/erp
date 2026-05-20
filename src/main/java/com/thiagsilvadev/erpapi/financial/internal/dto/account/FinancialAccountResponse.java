package com.thiagsilvadev.erpapi.financial.internal.dto.account;

import java.util.UUID;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccountType;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialResourceStatus;

public record FinancialAccountResponse(
        UUID id, String name, FinancialAccountType type, String description, FinancialResourceStatus status) {}
