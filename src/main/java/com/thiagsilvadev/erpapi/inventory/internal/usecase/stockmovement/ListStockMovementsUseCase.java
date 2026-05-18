package com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement;

import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;

import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;

@Component
public class ListStockMovementsUseCase {

    private final StockMovementRepository movementRepository;
    private final InventoryMapper mapper;

    public ListStockMovementsUseCase(StockMovementRepository movementRepository, InventoryMapper mapper) {
        this.movementRepository = movementRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).READ)")
    public Page<StockMovementResponse> execute(Pageable pageable) {
        return movementRepository.findAll(pageable).map(mapper::toResponse);
    }
}
