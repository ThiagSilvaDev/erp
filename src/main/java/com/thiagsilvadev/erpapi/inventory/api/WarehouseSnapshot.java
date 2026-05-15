package com.thiagsilvadev.erpapi.inventory.api;

import java.util.UUID;

public record WarehouseSnapshot(UUID id, String code, String name) {}
