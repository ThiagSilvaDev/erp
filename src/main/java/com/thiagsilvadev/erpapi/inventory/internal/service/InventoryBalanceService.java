package com.thiagsilvadev.erpapi.inventory.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementType;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Service
public class InventoryBalanceService {

    private final InventoryItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final CatalogFacade catalogFacade;

    public InventoryBalanceService(
            InventoryItemRepository itemRepository,
            WarehouseRepository warehouseRepository,
            CatalogFacade catalogFacade) {
        this.itemRepository = itemRepository;
        this.warehouseRepository = warehouseRepository;
        this.catalogFacade = catalogFacade;
    }

    @Transactional
    public InventoryItem applyMovement(UUID productId, UUID warehouseId, int quantity, StockMovementType type) {
        catalogFacade.requireProductExistsById(productId);
        Warehouse warehouse = warehouseRepository
                .findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(Warehouse.class, warehouseId));
        return switch (type) {
            case IN, TRANSFER_IN, ADJUSTMENT ->
                itemRepository
                        .findByProductIdAndWarehouseId(productId, warehouseId)
                        .map(item -> {
                            item.increase(quantity);
                            return item;
                        })
                        .orElseGet(() -> itemRepository.save(InventoryItem.create(productId, warehouse, quantity)));
            case OUT, TRANSFER_OUT, RESERVE, RELEASE_RESERVATION -> {
                InventoryItem item = getExistingItem(productId, warehouseId);
                applyExistingItemMovement(item, quantity, type);
                yield item;
            }
        };
    }

    private InventoryItem getExistingItem(UUID productId, UUID warehouseId) {
        return itemRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryItem.class, productId + "/" + warehouseId));
    }

    private void applyExistingItemMovement(InventoryItem item, int quantity, StockMovementType type) {
        switch (type) {
            case OUT, TRANSFER_OUT -> item.decrease(quantity);
            case RESERVE -> item.reserve(quantity);
            case RELEASE_RESERVATION -> item.release(quantity);
            case IN, TRANSFER_IN, ADJUSTMENT ->
                throw new IllegalArgumentException("Movement type does not require existing item: " + type);
        }
    }
}
