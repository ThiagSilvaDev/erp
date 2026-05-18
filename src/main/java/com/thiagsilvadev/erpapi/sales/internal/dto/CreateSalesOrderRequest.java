package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateSalesOrderRequest(
        @NotNull UUID customerId, @Valid @NotEmpty List<CreateSalesOrderItemRequest> items) {}
