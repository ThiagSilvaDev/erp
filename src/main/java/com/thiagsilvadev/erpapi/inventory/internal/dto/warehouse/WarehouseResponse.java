package com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse;

import java.util.UUID;

public record WarehouseResponse(UUID id, String code, String name) {}
