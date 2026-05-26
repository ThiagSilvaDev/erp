package com.thiagsilvadev.erpapi.inventory.api;

import java.util.UUID;

public record SalesOrderReference(UUID id, Long orderNumber) {}
