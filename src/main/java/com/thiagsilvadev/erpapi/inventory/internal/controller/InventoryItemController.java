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

    private final ListInventoryItemsUseCase listInventoryItemsUseCase;
    private final GetInventoryItemByIdUseCase getInventoryItemByIdUseCase;

    InventoryItemController(
            ListInventoryItemsUseCase listInventoryItemsUseCase,
            GetInventoryItemByIdUseCase getInventoryItemByIdUseCase) {
        this.listInventoryItemsUseCase = listInventoryItemsUseCase;
        this.getInventoryItemByIdUseCase = getInventoryItemByIdUseCase;
    }

    @Override
    public Page<InventoryItemResponse> listItems(Pageable pageable) {
        return listInventoryItemsUseCase.execute(pageable);
    }

    @Override
    public InventoryItemResponse getItem(@PathVariable UUID id) {
        return getInventoryItemByIdUseCase.execute(id);
    }
}
