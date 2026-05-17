package com.thiagsilvadev.erpapi.inventory.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Component
public class ListWarehousesUseCase {

    private final WarehouseRepository warehouseRepository;
    private final InventoryMapper mapper;

    public ListWarehousesUseCase(WarehouseRepository warehouseRepository, InventoryMapper mapper) {
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<WarehouseResponse> execute(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(mapper::toResponse);
    }
}
