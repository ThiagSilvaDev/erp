package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderStatus;

public record SalesOrderSearchCriteria(
        SaleType saleType, UUID sellerCompanyId, UUID buyerCompanyId, UUID customerId, SalesOrderStatus status) {}
