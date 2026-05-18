package com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;

@Component
public class GetStockMovementByIdUseCase {

    private final StockMovementRepository movementRepository;
    private final InventoryMapper mapper;

    public GetStockMovementByIdUseCase(StockMovementRepository movementRepository, InventoryMapper mapper) {
        this.movementRepository = movementRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).READ)")
    public StockMovementResponse execute(UUID id) {
        return mapper.toResponse(movementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(StockMovement.class, id)));
    }
}
