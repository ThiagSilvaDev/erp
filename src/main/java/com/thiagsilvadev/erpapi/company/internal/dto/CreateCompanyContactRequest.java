package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCompanyContactRequest(
        @NotBlank String name, String role, @Valid @NotNull ContactRequest contact, Boolean primaryContact) {}
