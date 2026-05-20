package com.thiagsilvadev.erpapi.sales.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SalesOrderConfirmedEvent(
        UUID salesOrderId,
        SaleType saleType,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        BigDecimal totalAmount,
        String currencyCode,
        LocalDate confirmedAt) {}
