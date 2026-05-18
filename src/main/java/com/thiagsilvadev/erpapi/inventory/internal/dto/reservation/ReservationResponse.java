package com.thiagsilvadev.erpapi.inventory.internal.dto.reservation;

import java.util.UUID;

public record ReservationResponse(UUID id, UUID productId, UUID warehouseId, int quantity, UUID salesOrderId) {}
