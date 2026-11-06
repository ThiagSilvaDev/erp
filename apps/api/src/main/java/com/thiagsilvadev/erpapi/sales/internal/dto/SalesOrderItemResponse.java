package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderItemResponse(
        UUID id,
        UUID productId,
        UUID warehouseId,
        String productSku,
        String productName,
        int quantity,
        BigDecimal unitPriceAmount,
        String currencyCode,
        BigDecimal lineTotal) {}
