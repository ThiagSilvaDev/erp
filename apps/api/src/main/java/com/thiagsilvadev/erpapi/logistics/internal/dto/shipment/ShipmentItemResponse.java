package com.thiagsilvadev.erpapi.logistics.internal.dto.shipment;

import java.util.UUID;

public record ShipmentItemResponse(
        UUID productId, UUID warehouseId, String productSku, String productName, int quantity) {}
