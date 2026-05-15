package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateContactPersonRequest(
        @NotBlank String name, @NotNull UUID customerId, @Valid @NotNull ContactRequest contact) {}
