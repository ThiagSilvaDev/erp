package com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse;

import org.mapstruct.Mapper;

import com.thiagsilvadev.erpapi.inventory.api.WarehouseSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    WarehouseResponse toResponse(Warehouse warehouse);

    WarehouseSnapshot toSnapshot(Warehouse warehouse);
}
