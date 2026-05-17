package com.thiagsilvadev.erpapi.inventory.internal.usecase;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.CreateWarehouseRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Component
public class CreateWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;
    private final InventoryMapper mapper;

    public CreateWarehouseUseCase(WarehouseRepository warehouseRepository, InventoryMapper mapper) {
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    @Transactional
    public WarehouseResponse execute(CreateWarehouseRequest request) {
        String code = Warehouse.normalizeCode(request.code());
        if (warehouseRepository.existsByCode(code)) {
            throw new ConflictException("Warehouse with this code already exists.", java.util.Map.of("code", code));
        }
        Warehouse savedWarehouse;
        try {
            savedWarehouse = warehouseRepository.saveAndFlush(Warehouse.create(code, request.name()));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Warehouse with this code already exists.", java.util.Map.of("code", code));
        }
        return mapper.toResponse(savedWarehouse);
    }
}
