package com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementResponse;
import com.thiagsilvadev.erpapi.inventory.internal.mapper.StockMovementMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;

@Component
public class GetStockMovementByIdUseCase {

    private final StockMovementRepository movementRepository;
    private final StockMovementMapper mapper;

    public GetStockMovementByIdUseCase(StockMovementRepository movementRepository, StockMovementMapper mapper) {
        this.movementRepository = movementRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public StockMovementResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(movementRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(StockMovement.class, id)));
    }
}
