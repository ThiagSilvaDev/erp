package com.thiagsilvadev.erpapi.financial.internal.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccountType;

public record CreateFinancialAccountRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull FinancialAccountType type,
        @Size(max = 120) String description) {}
