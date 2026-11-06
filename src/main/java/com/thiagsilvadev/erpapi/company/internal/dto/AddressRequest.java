package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record AddressRequest(
        @NotBlank @Schema(
                        description =
                                "8-digit CEP for BR addresses (formatted or digits-only), or up to 30 characters for other countries",
                        example = "01310100")
                String postalCode,
        @NotBlank String street,
        @NotBlank String number,
        String complement,
        @NotBlank String district,
        @NotBlank String city,
        @NotBlank @Size(min = 2, max = 2) @Schema(description = "2-letter Brazilian UF state code for BR addresses", example = "SP")
                String region,
        @NotBlank @Schema(
                        description = "Supported 2-letter ISO country code (BR, US, CN, AR, PY, UY, DE, PT, GB)",
                        example = "BR")
                String country) {}
