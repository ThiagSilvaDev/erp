package com.thiagsilvadev.erpapi.inventory.internal.usecase.stockmovement;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class StockMovementQueryUseCase {

    private final StockMovementRepository movementRepository;

    public StockMovementQueryUseCase(StockMovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public StockMovementResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return StockMovementMapper.toResponse(movementRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(StockMovement.class, id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<StockMovementResponse> list(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return movementRepository.findAllByCompanyId(companyId, pageable).map(StockMovementMapper::toResponse);
    }
}
