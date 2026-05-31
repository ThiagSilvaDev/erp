package com.thiagsilvadev.erpapi.customer.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record CreateSupplierRequest(
        @NotBlank String name,
        @NotBlank String document,
        @Valid ContactRequest contact,
        @Valid AddressRequest address) {}
