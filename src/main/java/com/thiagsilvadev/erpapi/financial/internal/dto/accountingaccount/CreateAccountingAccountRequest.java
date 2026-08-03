package com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccountType;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingSystemRole;

public record CreateAccountingAccountRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String description,
        UUID parentId,
        @NotNull AccountingAccountType type,
        AccountingSystemRole systemRole,
        Boolean acceptsPostings) {}
