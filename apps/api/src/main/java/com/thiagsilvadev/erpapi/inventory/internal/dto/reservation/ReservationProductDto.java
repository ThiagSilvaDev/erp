package com.thiagsilvadev.erpapi.inventory.internal.dto.reservation;

import java.util.UUID;

import com.thiagsilvadev.erpapi.common.domain.model.Money;

public record ReservationProductDto(UUID id, String sku, String name, Money basePrice) {}
