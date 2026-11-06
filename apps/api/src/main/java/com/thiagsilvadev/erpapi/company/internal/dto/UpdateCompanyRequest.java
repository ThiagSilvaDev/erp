package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.thiagsilvadev.erpapi.company.api.CompanyType;

public record UpdateCompanyRequest(
        @NotBlank String legalName,
        String tradeName,
        @NotNull CompanyType type,
        @Valid ContactRequest contact,
        @Valid @NotNull AddressRequest address) {}
