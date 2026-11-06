package com.thiagsilvadev.erpapi.inventory.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.reporting.ComparativePeriod;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryKpiSnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;

@Service
class InventoryKpiFacadeImpl implements InventoryKpiFacade {

    private final InventoryItemRepository itemRepository;
    private final StockMovementRepository movementRepository;
    private final InventoryKpiCalculator calculator;

    InventoryKpiFacadeImpl(
            InventoryItemRepository itemRepository,
            StockMovementRepository movementRepository,
            InventoryKpiCalculator calculator) {
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
        this.calculator = calculator;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryKpiSnapshot calculate(UUID companyId, ComparativePeriod period) {
        var currentRow = itemRepository.findKpiRow(companyId);
        var current = new InventoryKpiSnapshot.Current(
                currentRow.getItemCount(),
                currentRow.getQuantityOnHand(),
                currentRow.getReservedQuantity(),
                currentRow.getAvailableQuantity(),
                currentRow.getOutOfStockItems());
        var movements =
                movementRepository
                        .findKpiRows(companyId, period.queryFromInclusive(), period.queryToExclusive())
                        .stream()
                        .map(row -> new InventoryKpiCalculator.MovementRow(
                                row.getDate(),
                                row.getInbound(),
                                row.getOutbound(),
                                row.getReserved(),
                                row.getReleased(),
                                row.getAdjusted()))
                        .toList();
        return calculator.calculate(current, movements, period);
    }
}
