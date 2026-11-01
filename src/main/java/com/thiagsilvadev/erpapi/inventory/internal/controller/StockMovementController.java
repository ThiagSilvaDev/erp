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
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.*;
import com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement.*;

@RestController
class StockMovementController implements StockMovementApi {

    private final CreateStockMovementUseCase createStockMovementUseCase;
    private final StockMovementQueryUseCase stockMovementQueryUseCase;

    StockMovementController(
            CreateStockMovementUseCase createStockMovementUseCase,
            StockMovementQueryUseCase stockMovementQueryUseCase) {
        this.createStockMovementUseCase = createStockMovementUseCase;
        this.stockMovementQueryUseCase = stockMovementQueryUseCase;
    }

    @Override
    public ResponseEntity<StockMovementResponse> createMovement(
            @Valid @RequestBody CreateStockMovementRequest request) {
        StockMovementResponse response = createStockMovementUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public StockMovementResponse getMovement(@PathVariable UUID id) {
        return stockMovementQueryUseCase.getById(id);
    }

    @Override
    public Page<StockMovementResponse> listMovements(Pageable pageable) {
        return stockMovementQueryUseCase.list(pageable);
    }
}
