package com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementType;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Component
public class ApplyMovementUseCase {

    private final InventoryItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final CatalogFacade catalogFacade;

    public ApplyMovementUseCase(
            InventoryItemRepository itemRepository,
            WarehouseRepository warehouseRepository,
            CatalogFacade catalogFacade) {
        this.itemRepository = itemRepository;
        this.warehouseRepository = warehouseRepository;
        this.catalogFacade = catalogFacade;
    }

    @Transactional
    public InventoryItem execute(UUID productId, UUID warehouseId, int quantity, StockMovementType type) {
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
            case OUT, TRANSFER_OUT -> {
                InventoryItem item = getExistingItem(productId, warehouseId);
                item.decrease(quantity);
                yield item;
            }
            case RESERVE -> {
                InventoryItem item = getExistingItem(productId, warehouseId);
                item.reserve(quantity);
                yield item;
            }
            case RELEASE_RESERVATION -> {
                InventoryItem item = getExistingItem(productId, warehouseId);
                item.release(quantity);
                yield item;
            }
        };
    }

    private InventoryItem getExistingItem(UUID productId, UUID warehouseId) {
        return itemRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryItem.class, productId + "/" + warehouseId));
    }
}
