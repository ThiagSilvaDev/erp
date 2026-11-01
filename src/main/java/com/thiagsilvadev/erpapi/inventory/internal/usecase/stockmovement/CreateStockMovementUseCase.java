package com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.ADJUST_STOCK;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.CreateStockMovementRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementResponse;
import com.thiagsilvadev.erpapi.inventory.internal.mapper.StockMovementMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;
import com.thiagsilvadev.erpapi.inventory.internal.service.InventoryBalanceService;

@Component
public class CreateStockMovementUseCase {

    private final StockMovementRepository movementRepository;
    private final InventoryBalanceService inventoryBalanceService;
    private final BusinessNumberService businessNumberService;

    public CreateStockMovementUseCase(
            StockMovementRepository movementRepository,
            InventoryBalanceService inventoryBalanceService,
            BusinessNumberService businessNumberService) {
        this.movementRepository = movementRepository;
        this.inventoryBalanceService = inventoryBalanceService;
        this.businessNumberService = businessNumberService;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + ADJUST_STOCK + "')")
    public StockMovementResponse execute(CreateStockMovementRequest request) {
        var companyId = TenantContext.requireTenantId();
        InventoryItem item = inventoryBalanceService.applyMovement(
                companyId, request.productId(), request.warehouseId(), request.quantity(), request.type());
        StockMovement movement = movementRepository.save(StockMovement.create(
                businessNumberService.next(BusinessNumberType.STOCK_MOVEMENT),
                companyId,
                item.getProductId(),
                item.getWarehouse().getId(),
                request.quantity(),
                request.type(),
                request.reason()));
        return StockMovementMapper.toResponse(movement);
    }
}
