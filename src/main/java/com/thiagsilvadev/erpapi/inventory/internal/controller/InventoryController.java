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
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.*;
import com.thiagsilvadev.erpapi.inventory.internal.usecase.*;

@RestController
class InventoryController implements InventoryApi {

    private final CreateWarehouseUseCase createWarehouseUseCase;
    private final GetWarehouseByIdUseCase getWarehouseByIdUseCase;
    private final ListWarehousesUseCase listWarehousesUseCase;
    private final CreateStockMovementUseCase createStockMovementUseCase;
    private final GetStockMovementByIdUseCase getStockMovementByIdUseCase;
    private final ListStockMovementsUseCase listStockMovementsUseCase;
    private final ListInventoryItemsUseCase listInventoryItemsUseCase;
    private final GetInventoryItemByIdUseCase getInventoryItemByIdUseCase;
    private final CreateReservationUseCase createReservationUseCase;
    private final GetReservationByIdUseCase getReservationByIdUseCase;
    private final ListReservationsUseCase listReservationsUseCase;

    InventoryController(
            CreateWarehouseUseCase createWarehouseUseCase,
            GetWarehouseByIdUseCase getWarehouseByIdUseCase,
            ListWarehousesUseCase listWarehousesUseCase,
            CreateStockMovementUseCase createStockMovementUseCase,
            GetStockMovementByIdUseCase getStockMovementByIdUseCase,
            ListStockMovementsUseCase listStockMovementsUseCase,
            ListInventoryItemsUseCase listInventoryItemsUseCase,
            GetInventoryItemByIdUseCase getInventoryItemByIdUseCase,
            CreateReservationUseCase createReservationUseCase,
            GetReservationByIdUseCase getReservationByIdUseCase,
            ListReservationsUseCase listReservationsUseCase) {
        this.createWarehouseUseCase = createWarehouseUseCase;
        this.getWarehouseByIdUseCase = getWarehouseByIdUseCase;
        this.listWarehousesUseCase = listWarehousesUseCase;
        this.createStockMovementUseCase = createStockMovementUseCase;
        this.getStockMovementByIdUseCase = getStockMovementByIdUseCase;
        this.listStockMovementsUseCase = listStockMovementsUseCase;
        this.listInventoryItemsUseCase = listInventoryItemsUseCase;
        this.getInventoryItemByIdUseCase = getInventoryItemByIdUseCase;
        this.createReservationUseCase = createReservationUseCase;
        this.getReservationByIdUseCase = getReservationByIdUseCase;
        this.listReservationsUseCase = listReservationsUseCase;
    }

    @Override
    public ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        WarehouseResponse response = createWarehouseUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public WarehouseResponse getWarehouse(@PathVariable UUID id) {
        return getWarehouseByIdUseCase.execute(id);
    }

    @Override
    public Page<WarehouseResponse> listWarehouses(Pageable pageable) {
        return listWarehousesUseCase.execute(pageable);
    }

    @Override
    public ResponseEntity<StockMovementResponse> createMovement(
            @Valid @RequestBody CreateStockMovementRequest request) {
        StockMovementResponse response = createStockMovementUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public StockMovementResponse getMovement(@PathVariable UUID id) {
        return getStockMovementByIdUseCase.execute(id);
    }

    @Override
    public Page<StockMovementResponse> listMovements(Pageable pageable) {
        return listStockMovementsUseCase.execute(pageable);
    }

    @Override
    public Page<InventoryItemResponse> listItems(Pageable pageable) {
        return listInventoryItemsUseCase.execute(pageable);
    }

    @Override
    public InventoryItemResponse getItem(@PathVariable UUID id) {
        return getInventoryItemByIdUseCase.execute(id);
    }

    @Override
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = createReservationUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public ReservationResponse getReservation(@PathVariable UUID id) {
        return getReservationByIdUseCase.execute(id);
    }

    @Override
    public Page<ReservationResponse> listReservations(Pageable pageable) {
        return listReservationsUseCase.execute(pageable);
    }
}
