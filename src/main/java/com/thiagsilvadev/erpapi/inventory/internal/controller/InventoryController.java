package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.*;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.*;
import com.thiagsilvadev.erpapi.inventory.internal.repository.*;

@RestController
class InventoryController implements InventoryApi {

    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository itemRepository;
    private final StockMovementRepository movementRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryService inventoryService;
    private final InventoryMapper mapper;

    InventoryController(
            WarehouseRepository warehouseRepository,
            InventoryItemRepository itemRepository,
            StockMovementRepository movementRepository,
            InventoryReservationRepository reservationRepository,
            InventoryService inventoryService,
            InventoryMapper mapper) {
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        String code = Warehouse.normalizeCode(request.code());
        if (warehouseRepository.existsByCode(code)) {
            throw new ConflictException("Warehouse with this code already exists.", java.util.Map.of("code", code));
        }
        WarehouseResponse response =
                mapper.toResponse(warehouseRepository.save(Warehouse.create(code, request.name())));
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouse(@PathVariable UUID id) {
        return mapper.toResponse(
                warehouseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Warehouse.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseResponse> listWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ResponseEntity<StockMovementResponse> createMovement(
            @Valid @RequestBody CreateStockMovementRequest request) {
        InventoryItem item =
                inventoryService.applyMovement(request.productId(), request.warehouseId(), request.quantity());
        StockMovement movement = movementRepository.save(StockMovement.create(
                item.getProductId(), item.getWarehouse().getId(), request.quantity(), request.reason()));
        return ResponseEntities.created(movement.getId(), mapper.toResponse(movement));
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovementResponse getMovement(@PathVariable UUID id) {
        return mapper.toResponse(movementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(StockMovement.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> listMovements(Pageable pageable) {
        return movementRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> listItems(Pageable pageable) {
        return itemRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponse getItem(@PathVariable UUID id) {
        return mapper.toResponse(
                itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(InventoryItem.class, id)));
    }

    @Override
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        var reservation = inventoryService.reserve(
                request.productId(), request.warehouseId(), request.quantity(), request.salesOrderId());
        return ResponseEntities.created(
                reservation.id(),
                new ReservationResponse(
                        reservation.id(),
                        reservation.productId(),
                        reservation.warehouseId(),
                        reservation.quantity(),
                        reservation.salesOrderId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(@PathVariable UUID id) {
        return mapper.toResponse(reservationRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryReservation.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> listReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable).map(mapper::toResponse);
    }
}
