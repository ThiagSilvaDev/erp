package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public record SalesOrderCancelledEvent(UUID salesOrderId) {}
