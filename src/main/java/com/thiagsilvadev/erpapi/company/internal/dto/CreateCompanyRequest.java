package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.br.CNPJ;

import com.thiagsilvadev.erpapi.company.api.CompanyType;

public record CreateCompanyRequest(
        @NotBlank String legalName,
        String tradeName,
        @NotBlank @CNPJ String cnpj,
        @NotNull CompanyType type,
        @Valid ContactRequest contact,
        @Valid @NotNull AddressRequest address) {}
