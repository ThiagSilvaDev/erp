package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.thiagsilvadev.erpapi.sales.api.OrderStatus;

public final class SalesDtos {

    private SalesDtos() {}

    public record CreateSalesOrderRequest(
            @NotNull UUID customerId, @Valid @NotEmpty List<CreateSalesOrderItemRequest> items) {}

    public record CreateSalesOrderItemRequest(
            @NotNull UUID productId, @NotNull UUID warehouseId, @Positive int quantity) {}

    public record SalesOrderResponse(
            UUID id,
            UUID customerId,
            OrderStatus status,
            BigDecimal totalAmount,
            String currencyCode,
            List<SalesOrderItemResponse> items) {}

    public record SalesOrderItemResponse(
            UUID productId,
            UUID warehouseId,
            String productSku,
            String productName,
            int quantity,
            BigDecimal unitPriceAmount,
            String currencyCode,
            BigDecimal lineTotal) {}

    public record SalesOrderSearchCriteria(UUID customerId, OrderStatus status) {}
}
