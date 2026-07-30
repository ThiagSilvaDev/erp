package com.thiagsilvadev.erpapi.logistics.internal.dto.driver;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDriverRequest(@NotNull UUID userId, @NotBlank String licenseNumber) {}
