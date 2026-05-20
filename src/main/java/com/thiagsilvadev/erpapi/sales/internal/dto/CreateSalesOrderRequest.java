package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.thiagsilvadev.erpapi.sales.api.SaleType;

public record CreateSalesOrderRequest(
        @NotNull SaleType saleType,
        @NotNull UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        @Valid @NotEmpty List<CreateSalesOrderItemRequest> items) {}
