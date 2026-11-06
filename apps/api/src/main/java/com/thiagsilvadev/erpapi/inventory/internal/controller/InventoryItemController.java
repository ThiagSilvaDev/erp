package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.inventory.internal.dto.item.*;
import com.thiagsilvadev.erpapi.inventory.internal.usecase.item.*;

@RestController
class InventoryItemController implements InventoryItemApi {

    private final InventoryItemQueryUseCase inventoryItemQueryUseCase;

    InventoryItemController(InventoryItemQueryUseCase inventoryItemQueryUseCase) {
        this.inventoryItemQueryUseCase = inventoryItemQueryUseCase;
    }

    @Override
    public Page<InventoryItemResponse> list(Pageable pageable) {
        return inventoryItemQueryUseCase.list(pageable);
    }

    @Override
    public InventoryItemResponse getById(@PathVariable UUID id) {
        return inventoryItemQueryUseCase.getById(id);
    }
}
