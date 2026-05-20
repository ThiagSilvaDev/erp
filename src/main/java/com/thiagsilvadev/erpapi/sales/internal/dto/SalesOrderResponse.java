package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.OrderStatus;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.api.SaleType;

public record SalesOrderResponse(
        UUID id,
        Long orderNumber,
        SaleType saleType,
        FulfillmentType fulfillmentType,
        PaymentMethod paymentMethod,
        DeliveryRouteRequest deliveryRoute,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currencyCode,
        List<SalesOrderItemResponse> items) {}
