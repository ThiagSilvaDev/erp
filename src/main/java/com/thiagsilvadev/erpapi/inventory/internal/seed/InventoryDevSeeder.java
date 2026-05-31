package com.thiagsilvadev.erpapi.inventory.internal.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementType;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;
import com.thiagsilvadev.erpapi.inventory.internal.service.InventoryBalanceService;

@Component
@Profile("dev")
@Order(500)
class InventoryDevSeeder implements ApplicationRunner {

    static final String MAIN_WAREHOUSE_CODE = "MAIN";
    private static final String INDUSTRIAL_WIDGET_SKU = "SKU-001";
    private static final String ACME_CNPJ = "97.986.234/0001-91";

    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository movementRepository;
    private final CatalogFacade catalogFacade;
    private final CompanyFacade companyFacade;
    private final InventoryBalanceService inventoryBalanceService;

    InventoryDevSeeder(
            WarehouseRepository warehouseRepository,
            StockMovementRepository movementRepository,
            CatalogFacade catalogFacade,
            CompanyFacade companyFacade,
            InventoryBalanceService inventoryBalanceService) {
        this.warehouseRepository = warehouseRepository;
        this.movementRepository = movementRepository;
        this.catalogFacade = catalogFacade;
        this.companyFacade = companyFacade;
        this.inventoryBalanceService = inventoryBalanceService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var companyId = companyFacade
                .findByCnpj(ACME_CNPJ)
                .orElseThrow(() -> new IllegalStateException("Seed company must exist before seeding inventory"))
                .id();
        Warehouse warehouse = warehouseRepository
                .findByCompanyIdAndCode(companyId, Warehouse.normalizeCode(MAIN_WAREHOUSE_CODE))
                .orElseGet(() ->
                        warehouseRepository.save(Warehouse.create(companyId, MAIN_WAREHOUSE_CODE, "Main Warehouse")));
        if (movementRepository.count() > 0) {
            return;
        }
        var product = catalogFacade
                .findProductBySku(INDUSTRIAL_WIDGET_SKU, companyId)
                .orElseThrow(() -> new IllegalStateException("Seed product must exist before seeding inventory"));
        inventoryBalanceService.applyMovement(companyId, product.id(), warehouse.getId(), 100, StockMovementType.IN);
    }
}
