package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.thiagsilvadev.erpapi.sales.api.OrderStatus;

public record SalesOrderResponse(
        UUID id,
        Long orderNumber,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currencyCode,
        List<SalesOrderItemResponse> items) {}
