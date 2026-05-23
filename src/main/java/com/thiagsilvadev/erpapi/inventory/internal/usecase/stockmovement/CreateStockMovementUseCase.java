package com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.CreateStockMovementRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;

@Component
public class CreateStockMovementUseCase {

    private final StockMovementRepository movementRepository;
    private final ApplyMovementUseCase applyMovementUseCase;
    private final InventoryMapper mapper;
    private final BusinessNumberService businessNumberService;

    public CreateStockMovementUseCase(
            StockMovementRepository movementRepository,
            ApplyMovementUseCase applyMovementUseCase,
            InventoryMapper mapper,
            BusinessNumberService businessNumberService) {
        this.movementRepository = movementRepository;
        this.applyMovementUseCase = applyMovementUseCase;
        this.mapper = mapper;
        this.businessNumberService = businessNumberService;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions).WRITE)")
    public StockMovementResponse execute(CreateStockMovementRequest request) {
        InventoryItem item = applyMovementUseCase.execute(
                request.productId(), request.warehouseId(), request.quantity(), request.type());
        StockMovement movement = movementRepository.save(StockMovement.create(
                businessNumberService.next(BusinessNumberType.STOCK_MOVEMENT),
                item.getProductId(),
                item.getWarehouse().getId(),
                request.quantity(),
                request.type(),
                request.reason()));
        return mapper.toResponse(movement);
    }
}
