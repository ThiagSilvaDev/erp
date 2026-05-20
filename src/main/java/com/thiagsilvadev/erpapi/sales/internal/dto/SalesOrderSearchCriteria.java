package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.sales.api.OrderStatus;
import com.thiagsilvadev.erpapi.sales.api.SaleType;

public record SalesOrderSearchCriteria(
        SaleType saleType, UUID sellerCompanyId, UUID buyerCompanyId, UUID customerId, OrderStatus status) {}
