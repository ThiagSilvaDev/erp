package com.thiagsilvadev.erpapi.sales.api;

import java.util.UUID;

public record SalesOrderDeliveryRouteSnapshot(
        String routeName, UUID originWarehouseId, String destinationDescription) {}
