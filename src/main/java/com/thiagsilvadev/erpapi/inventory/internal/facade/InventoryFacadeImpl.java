package com.thiagsilvadev.erpapi.inventory.internal.facade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.api.*;
import com.thiagsilvadev.erpapi.inventory.internal.mapper.ReservationMapper;
import com.thiagsilvadev.erpapi.inventory.internal.mapper.WarehouseMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.*;
import com.thiagsilvadev.erpapi.inventory.internal.repository.*;

@Service
public class InventoryFacadeImpl implements InventoryFacade {

    private final InventoryItemRepository itemRepository;
    private final InventoryReservationRepository reservationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ReservationMapper reservationMapper;
    private final WarehouseMapper warehouseMapper;

    public InventoryFacadeImpl(
            InventoryItemRepository itemRepository,
            InventoryReservationRepository reservationRepository,
            WarehouseRepository warehouseRepository,
            ReservationMapper reservationMapper,
            WarehouseMapper warehouseMapper) {
        this.itemRepository = itemRepository;
        this.reservationRepository = reservationRepository;
        this.warehouseRepository = warehouseRepository;
        this.reservationMapper = reservationMapper;
        this.warehouseMapper = warehouseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseSnapshot resolveWarehouse(UUID companyId, String code) {
        return warehouseRepository
                .findByCompanyIdAndCode(companyId, Warehouse.normalizeCode(code))
                .map(warehouseMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(Warehouse.class, code));
    }

    @Override
    @Transactional
    public InventoryReservationSnapshot reserveStock(ReserveStockCommand command) {
        InventoryItem item = itemRepository
                .findByCompanyIdAndProductIdAndWarehouseId(
                        command.companyId(), command.productId(), command.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        InventoryItem.class, command.productId() + "/" + command.warehouseId()));
        item.reserve(command.quantity());
        InventoryReservation reservation = reservationRepository.save(InventoryReservation.create(
                command.companyId(),
                command.productId(),
                command.warehouseId(),
                command.quantity(),
                command.salesOrderId()));
        return reservationMapper.toSnapshot(reservation);
    }

    @Override
    @Transactional
    public void releaseReservationsForSalesOrder(UUID companyId, UUID salesOrderId) {
        for (InventoryReservation reservation :
                reservationRepository.findByCompanyIdAndSalesOrderId(companyId, salesOrderId)) {
            itemRepository
                    .findByCompanyIdAndProductIdAndWarehouseId(
                            companyId, reservation.getProductId(), reservation.getWarehouseId())
                    .ifPresent(item -> item.release(reservation.getQuantity()));
            reservationRepository.delete(reservation);
        }
    }

    @Override
    @Transactional
    public void consumeReservationsForSalesOrder(UUID companyId, UUID salesOrderId) {
        for (InventoryReservation reservation :
                reservationRepository.findByCompanyIdAndSalesOrderId(companyId, salesOrderId)) {
            InventoryItem item = itemRepository
                    .findByCompanyIdAndProductIdAndWarehouseId(
                            companyId, reservation.getProductId(), reservation.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            InventoryItem.class, reservation.getProductId() + "/" + reservation.getWarehouseId()));
            item.consumeReserved(reservation.getQuantity());
            reservationRepository.delete(reservation);
        }
    }
}
