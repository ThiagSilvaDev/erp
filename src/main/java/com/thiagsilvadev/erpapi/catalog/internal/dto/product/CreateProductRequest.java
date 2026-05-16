package com.thiagsilvadev.erpapi.catalog.internal.dto.product;

import java.util.UUID;

import com.thiagsilvadev.erpapi.catalog.internal.dto.MoneyRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @Valid @NotNull MoneyRequest basePrice,
        @NotNull UUID categoryId) {}
