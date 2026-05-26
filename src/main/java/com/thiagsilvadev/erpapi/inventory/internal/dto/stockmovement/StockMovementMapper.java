package com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement;

import org.mapstruct.Mapper;

import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    StockMovementResponse toResponse(StockMovement movement);
}
