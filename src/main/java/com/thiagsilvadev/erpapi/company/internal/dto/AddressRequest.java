package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank String postalCode,
        @NotBlank String street,
        @NotBlank String number,
        String complement,
        @NotBlank String district,
        @NotBlank String city,
        @NotBlank @Size(min = 2, max = 2) String region,
        @NotBlank String country) {}
