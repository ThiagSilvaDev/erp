package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.br.CNPJ;

import com.thiagsilvadev.erpapi.company.api.CompanyType;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCompanyRequest(
        @NotBlank String legalName,
        String tradeName,
        @NotBlank @CNPJ
                @Schema(
                        description = "14-digit Brazilian CNPJ (formatted or digits-only; stored as 14 digits)",
                        example = "11222333000181")
                String cnpj,
        @NotNull CompanyType type,
        @Valid ContactRequest contact,
        @Valid @NotNull AddressRequest address) {}
