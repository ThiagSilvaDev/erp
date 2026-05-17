package com.thiagsilvadev.erpapi.inventory.internal.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Component
public class GetWarehouseByIdUseCase {

    private final WarehouseRepository warehouseRepository;
    private final InventoryMapper mapper;

    public GetWarehouseByIdUseCase(WarehouseRepository warehouseRepository, InventoryMapper mapper) {
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public WarehouseResponse execute(UUID id) {
        return mapper.toResponse(
                warehouseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Warehouse.class, id)));
    }
}
