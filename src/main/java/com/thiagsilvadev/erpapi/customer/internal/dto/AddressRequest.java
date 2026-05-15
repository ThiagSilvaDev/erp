package com.thiagsilvadev.erpapi.customer.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String postalCode,
        @NotBlank String street,
        @NotBlank String number,
        String complement,
        @NotBlank String district,
        @NotBlank String city,
        @NotBlank String region,
        @NotBlank String country) {}
