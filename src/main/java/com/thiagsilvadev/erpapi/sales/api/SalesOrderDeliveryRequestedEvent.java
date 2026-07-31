package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public record SalesOrderDeliveryRequestedEvent(UUID salesOrderId, UUID sellerCompanyId) {}
