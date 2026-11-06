package com.thiagsilvadev.erpapi.logistics.internal.dto.driver;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateDriverRequest(
        @NotNull UUID userId,
        @NotBlank @Schema(
                        description = "Driver license number, trimmed and normalized to uppercase (max 60 characters)",
                        example = "12345678900")
                String licenseNumber) {}
