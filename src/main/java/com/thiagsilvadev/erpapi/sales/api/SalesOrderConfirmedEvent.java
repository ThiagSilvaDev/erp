package com.thiagsilvadev.erpapi.sales.api;

import java.time.LocalDate;
import java.util.UUID;

public record SalesOrderConfirmedEvent(UUID salesOrderId, LocalDate confirmedAt) {}
