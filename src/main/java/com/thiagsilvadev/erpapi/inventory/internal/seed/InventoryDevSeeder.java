package com.thiagsilvadev.erpapi.inventory.internal.seed;

import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;
import com.thiagsilvadev.erpapi.inventory.internal.service.InventoryService;

@Component
@Profile("dev")
@Order(500)
class InventoryDevSeeder implements ApplicationRunner {

    static final String MAIN_WAREHOUSE_CODE = "MAIN";
    private static final String INDUSTRIAL_WIDGET_SKU = "SKU-001";

    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository movementRepository;
    private final CatalogFacade catalogFacade;
    private final InventoryService inventoryService;

    InventoryDevSeeder(
            WarehouseRepository warehouseRepository,
            StockMovementRepository movementRepository,
            CatalogFacade catalogFacade,
            InventoryService inventoryService) {
        this.warehouseRepository = warehouseRepository;
        this.movementRepository = movementRepository;
        this.catalogFacade = catalogFacade;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Warehouse warehouse = warehouseRepository
                .findByCode(Warehouse.normalizeCode(MAIN_WAREHOUSE_CODE))
                .orElseGet(() -> warehouseRepository.save(Warehouse.create(MAIN_WAREHOUSE_CODE, "Main Warehouse")));
        if (movementRepository.count() > 0) {
            return;
        }
        var product = catalogFacade
                .findProductBySku(INDUSTRIAL_WIDGET_SKU)
                .orElseThrow(() -> new IllegalStateException("Seed product must exist before seeding inventory"));
        inventoryService.applyMovement(product.id(), warehouse.getId(), 100, StockMovementType.IN);
    }
}
