package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
        @NotBlank String name,
        @NotBlank String document,
        @NotNull UUID companyId,
        @Valid ContactRequest contact,
        @Valid AddressRequest address) {}
