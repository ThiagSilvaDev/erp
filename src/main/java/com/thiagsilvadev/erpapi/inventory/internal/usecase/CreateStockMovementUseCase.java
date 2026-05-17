package com.thiagsilvadev.erpapi.inventory.internal.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.inventory.internal.controller.InventoryService;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.CreateStockMovementRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.StockMovementResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;

@Component
public class CreateStockMovementUseCase {

    private final StockMovementRepository movementRepository;
    private final InventoryService inventoryService;
    private final InventoryMapper mapper;

    public CreateStockMovementUseCase(
            StockMovementRepository movementRepository, InventoryService inventoryService, InventoryMapper mapper) {
        this.movementRepository = movementRepository;
        this.inventoryService = inventoryService;
        this.mapper = mapper;
    }

    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).WRITE)")
    public StockMovementResponse execute(CreateStockMovementRequest request) {
        InventoryItem item =
                inventoryService.applyMovement(request.productId(), request.warehouseId(), request.quantity());
        StockMovement movement = movementRepository.save(StockMovement.create(
                item.getProductId(), item.getWarehouse().getId(), request.quantity(), request.reason()));
        return mapper.toResponse(movement);
    }
}
