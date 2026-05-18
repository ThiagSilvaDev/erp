package com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse;

import jakarta.validation.constraints.NotBlank;

public record CreateWarehouseRequest(@NotBlank String code, @NotBlank String name) {}
