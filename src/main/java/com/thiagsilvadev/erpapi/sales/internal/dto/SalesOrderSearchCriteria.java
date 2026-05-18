package com.thiagsilvadev.erpapi.sales.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.sales.api.OrderStatus;

public record SalesOrderSearchCriteria(UUID customerId, OrderStatus status) {}
