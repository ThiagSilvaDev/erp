package com.thiagsilvadev.erpapi.sales.api;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import com.thiagsilvadev.erpapi.sales.internal.dto.SalesOrderItemResponse;

public record SalesOrderSnapshot(
        UUID id,
        Long orderNumber,
        SaleType saleType,
        FulfillmentType fulfillmentType,
        PaymentMethod paymentMethod,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currencyCode,
        Set<SalesOrderItemResponse> items) {}
