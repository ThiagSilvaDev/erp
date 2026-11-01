package com.thiagsilvadev.erpapi.inventory.internal.mapper;

import com.thiagsilvadev.erpapi.inventory.api.WarehouseSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;

public final class WarehouseMapper {

    private WarehouseMapper() {}

    public static WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(warehouse.getId(), warehouse.getCode(), warehouse.getName());
    }

    public static WarehouseSnapshot toSnapshot(Warehouse warehouse) {
        return new WarehouseSnapshot(warehouse.getId(), warehouse.getCode(), warehouse.getName());
    }
}
