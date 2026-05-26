package com.thiagsilvadev.erpapi.inventory.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.api.*;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.reservation.ReservationMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.*;
import com.thiagsilvadev.erpapi.inventory.internal.repository.*;

@Service
public class InventoryService implements InventoryFacade {

    private final InventoryItemRepository itemRepository;
    private final InventoryReservationRepository reservationRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemMapper itemMapper;
    private final ReservationMapper reservationMapper;
    private final WarehouseMapper warehouseMapper;

    public InventoryService(
            InventoryItemRepository itemRepository,
            InventoryReservationRepository reservationRepository,
            WarehouseRepository warehouseRepository,
            InventoryItemMapper itemMapper,
            ReservationMapper reservationMapper,
            WarehouseMapper warehouseMapper) {
        this.itemRepository = itemRepository;
        this.reservationRepository = reservationRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemMapper = itemMapper;
        this.reservationMapper = reservationMapper;
        this.warehouseMapper = warehouseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItemSnapshot> findItem(UUID productId, UUID warehouseId) {
        return itemRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .map(itemMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WarehouseSnapshot> findWarehouseByCode(String code) {
        return warehouseRepository.findByCode(Warehouse.normalizeCode(code)).map(warehouseMapper::toSnapshot);
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
        return reservationMapper.toSnapshot(reservation);
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

    @Override
    @Transactional
    public void consumeReservationsForSalesOrder(UUID salesOrderId) {
        for (InventoryReservation reservation : reservationRepository.findBySalesOrderId(salesOrderId)) {
            InventoryItem item = itemRepository
                    .findByProductIdAndWarehouseId(reservation.getProductId(), reservation.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            InventoryItem.class, reservation.getProductId() + "/" + reservation.getWarehouseId()));
            item.consumeReserved(reservation.getQuantity());
            reservationRepository.delete(reservation);
        }
    }
}
