package com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.stockmovement.StockMovementResponse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;

@Component
public class ListStockMovementsUseCase {

    private final StockMovementRepository movementRepository;
    private final StockMovementMapper mapper;

    public ListStockMovementsUseCase(StockMovementRepository movementRepository, StockMovementMapper mapper) {
        this.movementRepository = movementRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<StockMovementResponse> execute(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return movementRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
