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
    public InventoryItem applyMovement(
            UUID companyId, UUID productId, UUID warehouseId, int quantity, StockMovementType type) {
        catalogFacade.getProduct(companyId, productId);
        Warehouse warehouse = warehouseRepository
                .findByIdAndCompanyId(warehouseId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Warehouse.class, warehouseId));
        return itemRepository
                .findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId)
                .map(item -> {
                    type.applyTo(item, quantity);
                    return item;
                })
                .orElseGet(() -> {
                    if (!type.canCreateItem()) {
                        throw new ResourceNotFoundException(InventoryItem.class, productId + "/" + warehouseId);
                    }
                    return itemRepository.save(InventoryItem.create(productId, warehouse, quantity));
                });
    }
}
