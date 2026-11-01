package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.*;
import com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse.*;

@RestController
class WarehouseController implements WarehouseApi {

    private final CreateWarehouseUseCase createWarehouseUseCase;
    private final WarehouseQueryUseCase warehouseQueryUseCase;

    WarehouseController(CreateWarehouseUseCase createWarehouseUseCase, WarehouseQueryUseCase warehouseQueryUseCase) {
        this.createWarehouseUseCase = createWarehouseUseCase;
        this.warehouseQueryUseCase = warehouseQueryUseCase;
    }

    @Override
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody CreateWarehouseRequest request) {
        WarehouseResponse response = createWarehouseUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public WarehouseResponse getById(@PathVariable UUID id) {
        return warehouseQueryUseCase.getById(id);
    }

    @Override
    public Page<WarehouseResponse> list(Pageable pageable) {
        return warehouseQueryUseCase.list(pageable);
    }
}
