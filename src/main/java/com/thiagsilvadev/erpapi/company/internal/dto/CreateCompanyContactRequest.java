package com.thiagsilvadev.erpapi.company.internal.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCompanyContactRequest(
        @NotNull UUID companyId,
        @NotBlank String name,
        String role,
        @Valid @NotNull ContactRequest contact,
        Boolean primaryContact) {}
