package com.thiagsilvadev.erpapi.inventory.internal.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.api.*;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.*;
import com.thiagsilvadev.erpapi.inventory.internal.repository.*;

@Service
public class InventoryService implements InventoryFacade {

    private final InventoryItemRepository itemRepository;
    private final InventoryReservationRepository reservationRepository;
    private final WarehouseRepository warehouseRepository;
    private final CatalogFacade catalogFacade;
    private final InventoryMapper mapper;

    public InventoryService(
            InventoryItemRepository itemRepository,
            InventoryReservationRepository reservationRepository,
            WarehouseRepository warehouseRepository,
            CatalogFacade catalogFacade,
            InventoryMapper mapper) {
        this.itemRepository = itemRepository;
        this.reservationRepository = reservationRepository;
        this.warehouseRepository = warehouseRepository;
        this.catalogFacade = catalogFacade;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItemSnapshot> findItem(UUID productId, UUID warehouseId) {
        return itemRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .map(mapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WarehouseSnapshot> findWarehouseByCode(String code) {
        return warehouseRepository.findByCode(Warehouse.normalizeCode(code)).map(mapper::toSnapshot);
    }

    @Override
    @Transactional
    public InventoryReservationSnapshot reserve(UUID productId, UUID warehouseId, int quantity, UUID salesOrderId) {
        InventoryItem item = itemRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryItem.class, productId + "/" + warehouseId));
        item.reserve(quantity);
        InventoryReservation reservation =
                reservationRepository.save(InventoryReservation.create(productId, warehouseId, quantity, salesOrderId));
        return mapper.toSnapshot(reservation);
    }

    @Override
    @Transactional
    public void releaseBySalesOrderId(UUID salesOrderId) {
        for (InventoryReservation reservation : reservationRepository.findBySalesOrderId(salesOrderId)) {
            itemRepository
                    .findByProductIdAndWarehouseId(reservation.getProductId(), reservation.getWarehouseId())
                    .ifPresent(item -> item.release(reservation.getQuantity()));
            reservationRepository.delete(reservation);
        }
    }

    @Transactional
    public InventoryItem applyMovement(UUID productId, UUID warehouseId, int quantity) {
        catalogFacade
                .findProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, productId));
        Warehouse warehouse = warehouseRepository
                .findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(Warehouse.class, warehouseId));
        return itemRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .map(item -> {
                    item.increase(quantity);
                    return item;
                })
                .orElseGet(() -> itemRepository.save(InventoryItem.create(productId, warehouse, quantity)));
    }
}
