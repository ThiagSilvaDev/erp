package com.thiagsilvadev.erpapi.sales.api;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record SalesOrderSnapshot(
        UUID id,
        Long orderNumber,
        SaleType saleType,
        FulfillmentType fulfillmentType,
        PaymentMethod paymentMethod,
        SalesOrderDeliveryRouteSnapshot deliveryRoute,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        SalesOrderStatus status,
        BigDecimal totalAmount,
        String currencyCode,
        Set<SalesOrderItemSnapshot> items) {}
