package com.thiagsilvadev.erpapi.inventory.internal.seed;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementReason;
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
    private static final String ACME_CNPJ = "97.986.234/0001-91";

    private static final List<MovementSeed> MOVEMENTS = List.of(
            new MovementSeed(58, "SKU-001", 200, StockMovementType.IN, StockMovementReason.INITIAL_STOCK),
            new MovementSeed(54, "SKU-002", 80, StockMovementType.IN, StockMovementReason.INITIAL_STOCK),
            new MovementSeed(50, "SKU-003", 120, StockMovementType.IN, StockMovementReason.INITIAL_STOCK),
            new MovementSeed(46, "SKU-004", 60, StockMovementType.IN, StockMovementReason.INITIAL_STOCK),
            new MovementSeed(44, "SKU-001", 25, StockMovementType.OUT, StockMovementReason.SALE_FULFILLED),
            new MovementSeed(40, "SKU-002", 10, StockMovementType.RESERVE, StockMovementReason.SALE_ORDER_CONFIRMED),
            new MovementSeed(
                    36, "SKU-002", 4, StockMovementType.RELEASE_RESERVATION, StockMovementReason.SALE_ORDER_CANCELLED),
            new MovementSeed(33, "SKU-003", 15, StockMovementType.ADJUSTMENT, StockMovementReason.INVENTORY_COUNT),
            new MovementSeed(27, "SKU-005", 40, StockMovementType.IN, StockMovementReason.PURCHASE_RECEIPT),
            new MovementSeed(24, "SKU-001", 50, StockMovementType.IN, StockMovementReason.PURCHASE_RECEIPT),
            new MovementSeed(21, "SKU-001", 35, StockMovementType.OUT, StockMovementReason.SALE_FULFILLED),
            new MovementSeed(18, "SKU-003", 20, StockMovementType.RESERVE, StockMovementReason.SALE_ORDER_CONFIRMED),
            new MovementSeed(
                    14, "SKU-002", 3, StockMovementType.RELEASE_RESERVATION, StockMovementReason.SALE_ORDER_CANCELLED),
            new MovementSeed(11, "SKU-004", 20, StockMovementType.TRANSFER_IN, StockMovementReason.WAREHOUSE_TRANSFER),
            new MovementSeed(8, "SKU-004", 15, StockMovementType.TRANSFER_OUT, StockMovementReason.WAREHOUSE_TRANSFER),
            new MovementSeed(5, "SKU-005", 8, StockMovementType.ADJUSTMENT, StockMovementReason.INVENTORY_COUNT),
            new MovementSeed(3, "SKU-006", 25, StockMovementType.IN, StockMovementReason.PURCHASE_RECEIPT),
            new MovementSeed(1, "SKU-006", 25, StockMovementType.OUT, StockMovementReason.SALE_FULFILLED));

    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository movementRepository;
    private final CatalogFacade catalogFacade;
    private final CompanyFacade companyFacade;
    private final InventoryBalanceService inventoryBalanceService;
    private final BusinessNumberService businessNumberService;
    private final Clock clock;

    InventoryDevSeeder(
            WarehouseRepository warehouseRepository,
            StockMovementRepository movementRepository,
            CatalogFacade catalogFacade,
            CompanyFacade companyFacade,
            InventoryBalanceService inventoryBalanceService,
            BusinessNumberService businessNumberService,
            Clock clock) {
        this.warehouseRepository = warehouseRepository;
        this.movementRepository = movementRepository;
        this.catalogFacade = catalogFacade;
        this.companyFacade = companyFacade;
        this.inventoryBalanceService = inventoryBalanceService;
        this.businessNumberService = businessNumberService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var companyId = companyFacade.resolveCompany(ACME_CNPJ).id();
        Warehouse warehouse = warehouseRepository
                .findByCompanyIdAndCode(companyId, Warehouse.normalizeCode(MAIN_WAREHOUSE_CODE))
                .orElseGet(() ->
                        warehouseRepository.save(Warehouse.create(companyId, MAIN_WAREHOUSE_CODE, "Main Warehouse")));
        if (movementRepository.countByCompanyId(companyId) >= MOVEMENTS.size()) {
            return;
        }
        LocalDate today = LocalDate.now(clock);

        for (MovementSeed seed : MOVEMENTS) {
            var product = catalogFacade.resolveProduct(companyId, seed.sku());
            inventoryBalanceService.applyMovement(
                    companyId, product.id(), warehouse.getId(), seed.quantity(), seed.type());
            StockMovement movement = movementRepository.saveAndFlush(StockMovement.create(
                    businessNumberService.next(BusinessNumberType.STOCK_MOVEMENT),
                    companyId,
                    product.id(),
                    warehouse.getId(),
                    seed.quantity(),
                    seed.type(),
                    seed.reason()));
            movementRepository.updateCreatedAtForDevSeed(
                    movement.getId(),
                    today.minusDays(seed.daysAgo()).atTime(12, 0).toInstant(ZoneOffset.UTC));
        }
    }

    static int seedMovementCount() {
        return MOVEMENTS.size();
    }

    private record MovementSeed(
            int daysAgo, String sku, int quantity, StockMovementType type, StockMovementReason reason) {}
}
