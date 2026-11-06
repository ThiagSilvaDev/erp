package com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccountType;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingSystemRole;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateAccountingAccountRequest(
        @NotBlank @Size(max = 40) @Schema(description = "Chart of accounts code, trimmed and unique per company", example = "1.1.01")
                String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String description,
        UUID parentId,
        @NotNull AccountingAccountType type,
        AccountingSystemRole systemRole,
        @Schema(
                        description =
                                "Whether the account accepts direct journal postings; defaults to true when omitted",
                        example = "true")
                Boolean acceptsPostings) {}
