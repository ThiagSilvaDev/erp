package com.thiagsilvadev.erpapi.sales.api;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderSnapshot(
        UUID id,
        Long orderNumber,
        SaleType saleType,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currencyCode) {}
