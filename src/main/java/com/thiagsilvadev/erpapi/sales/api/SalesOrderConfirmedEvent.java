package com.thiagsilvadev.erpapi.sales.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SalesOrderConfirmedEvent(
        UUID salesOrderId,
        SaleType saleType,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID customerId,
        BigDecimal totalAmount,
        String currencyCode,
        LocalDate confirmedAt,
        List<SalesOrderConfirmedItem> items) {

    public static SalesOrderConfirmedEvent confirmed(
            UUID salesOrderId,
            SaleType saleType,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            BigDecimal totalAmount,
            String currencyCode,
            LocalDate confirmedAt,
            List<SalesOrderConfirmedItem> items) {
        return new SalesOrderConfirmedEvent(
                salesOrderId,
                saleType,
                sellerCompanyId,
                buyerCompanyId,
                customerId,
                totalAmount,
                currencyCode,
                confirmedAt,
                items);
    }
}
