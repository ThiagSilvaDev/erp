package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thiagsilvadev.erpapi.common.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.*;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.*;
import com.thiagsilvadev.erpapi.inventory.internal.repository.*;

@RestController
@RequestMapping(value = "/api/v1/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
class InventoryController {

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

    @PostMapping("/warehouses")
    @Transactional
    ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        String code = Warehouse.normalizeCode(request.code());
        if (warehouseRepository.existsByCode(code)) {
            throw new ConflictException("Warehouse with this code already exists.", java.util.Map.of("code", code));
        }
        WarehouseResponse response =
                mapper.toResponse(warehouseRepository.save(Warehouse.create(code, request.name())));
        return created(response.id(), response);
    }

    @GetMapping("/warehouses/{id}")
    @Transactional(readOnly = true)
    WarehouseResponse getWarehouse(@PathVariable UUID id) {
        return mapper.toResponse(
                warehouseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Warehouse.class, id)));
    }

    @GetMapping("/warehouses")
    @Transactional(readOnly = true)
    Page<WarehouseResponse> listWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(mapper::toResponse);
    }

    @PostMapping("/stock-movements")
    @Transactional
    ResponseEntity<StockMovementResponse> createMovement(@Valid @RequestBody CreateStockMovementRequest request) {
        InventoryItem item =
                inventoryService.applyMovement(request.productId(), request.warehouseId(), request.quantity());
        StockMovement movement = movementRepository.save(StockMovement.create(
                item.getProductId(), item.getWarehouse().getId(), request.quantity(), request.reason()));
        return created(movement.getId(), mapper.toResponse(movement));
    }

    @GetMapping("/stock-movements/{id}")
    @Transactional(readOnly = true)
    StockMovementResponse getMovement(@PathVariable UUID id) {
        return mapper.toResponse(movementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(StockMovement.class, id)));
    }

    @GetMapping("/stock-movements")
    @Transactional(readOnly = true)
    Page<StockMovementResponse> listMovements(Pageable pageable) {
        return movementRepository.findAll(pageable).map(mapper::toResponse);
    }

    @GetMapping("/items")
    @Transactional(readOnly = true)
    Page<InventoryItemResponse> listItems(Pageable pageable) {
        return itemRepository.findAll(pageable).map(mapper::toResponse);
    }

    @GetMapping("/items/{id}")
    @Transactional(readOnly = true)
    InventoryItemResponse getItem(@PathVariable UUID id) {
        return mapper.toResponse(
                itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(InventoryItem.class, id)));
    }

    @PostMapping("/reservations")
    ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        var reservation = inventoryService.reserve(
                request.productId(), request.warehouseId(), request.quantity(), request.salesOrderId());
        return created(
                reservation.id(),
                new ReservationResponse(
                        reservation.id(),
                        reservation.productId(),
                        reservation.warehouseId(),
                        reservation.quantity(),
                        reservation.salesOrderId()));
    }

    @GetMapping("/reservations/{id}")
    @Transactional(readOnly = true)
    ReservationResponse getReservation(@PathVariable UUID id) {
        return mapper.toResponse(reservationRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryReservation.class, id)));
    }

    @GetMapping("/reservations")
    @Transactional(readOnly = true)
    Page<ReservationResponse> listReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable).map(mapper::toResponse);
    }

    private <T> ResponseEntity<T> created(UUID id, T body) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(body);
    }
}
