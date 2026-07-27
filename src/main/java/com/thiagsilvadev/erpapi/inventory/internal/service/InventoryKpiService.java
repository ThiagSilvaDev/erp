package com.thiagsilvadev.erpapi.inventory.internal.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;

@Service
class InventoryKpiService implements InventoryKpiFacade {

    private final InventoryItemRepository itemRepository;
    private final StockMovementRepository movementRepository;

    InventoryKpiService(InventoryItemRepository itemRepository, StockMovementRepository movementRepository) {
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryKpiSnapshot load(UUID companyId, Instant fromInclusive, Instant toExclusive) {
        var currentRow = itemRepository.findKpiRow(companyId);
        var current = new InventoryKpiSnapshot.Current(
                currentRow.getItemCount(),
                currentRow.getQuantityOnHand(),
                currentRow.getReservedQuantity(),
                currentRow.getAvailableQuantity(),
                currentRow.getOutOfStockItems());
        var movements = movementRepository.findKpiRows(companyId, fromInclusive, toExclusive).stream()
                .map(row -> new InventoryKpiSnapshot.DailyMovement(
                        row.getDate(),
                        row.getInbound(),
                        row.getOutbound(),
                        row.getReserved(),
                        row.getReleased(),
                        row.getAdjusted()))
                .toList();
        return new InventoryKpiSnapshot(current, movements);
    }
}
