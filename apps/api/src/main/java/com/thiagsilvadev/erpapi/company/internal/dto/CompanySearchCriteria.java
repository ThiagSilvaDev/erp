package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.constraints.Size;

import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;

import io.swagger.v3.oas.annotations.media.Schema;

public record CompanySearchCriteria(
        CompanyType type,
        CompanyStatus status,
        @Size(max = 24) @Schema(
                        description =
                                "14-digit Brazilian CNPJ prefix filter (formatted or digits-only; non-digits are stripped)",
                        example = "11222333000181")
                String cnpj,
        @Size(max = 160) String name) {}
