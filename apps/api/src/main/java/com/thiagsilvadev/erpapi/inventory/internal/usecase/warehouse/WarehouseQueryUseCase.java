package com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.mapper.WarehouseMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Component
public class WarehouseQueryUseCase {

    private final WarehouseRepository warehouseRepository;

    public WarehouseQueryUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public WarehouseResponse getById(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return WarehouseMapper.toResponse(warehouseRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Warehouse.class, id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<WarehouseResponse> list(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return warehouseRepository.findAllByCompanyId(companyId, pageable).map(WarehouseMapper::toResponse);
    }
}
