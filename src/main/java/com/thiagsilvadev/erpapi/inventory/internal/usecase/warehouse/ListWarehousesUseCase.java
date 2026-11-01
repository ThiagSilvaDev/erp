package com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Component
public class ListWarehousesUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper mapper;

    public ListWarehousesUseCase(WarehouseRepository warehouseRepository, WarehouseMapper mapper) {
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<WarehouseResponse> execute(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return warehouseRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
