package com.thiagsilvadev.erpapi.logistics.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDriverRequest(@NotBlank String name, @NotBlank String licenseNumber, @NotBlank String phone) {}
