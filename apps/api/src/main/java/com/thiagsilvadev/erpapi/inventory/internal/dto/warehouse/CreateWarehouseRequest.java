package com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateWarehouseRequest(
        @NotBlank @Schema(
                        description = "Warehouse code, trimmed and normalized to uppercase (max 40 characters)",
                        example = "WH-SP-01")
                String code,
        @NotBlank String name) {}
