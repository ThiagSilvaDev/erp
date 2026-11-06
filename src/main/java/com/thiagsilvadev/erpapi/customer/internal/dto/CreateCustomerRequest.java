package com.thiagsilvadev.erpapi.customer.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCustomerRequest(
        @NotBlank String name,
        @NotBlank @Schema(
                        description =
                                "Tax identification document (CPF/CNPJ); non-digits are stripped, up to 32 digits",
                        example = "12345678909")
                String document,
        @Valid ContactRequest contact,
        @Valid AddressRequest address) {}
