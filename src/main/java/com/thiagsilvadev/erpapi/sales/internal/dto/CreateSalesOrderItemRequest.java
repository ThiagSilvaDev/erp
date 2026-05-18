package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSalesOrderItemRequest(@NotNull UUID productId, @NotNull UUID warehouseId, @Positive int quantity) {}
