package com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.WRITE;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.CreateWarehouseRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.WarehouseResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

@Component
public class CreateWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper mapper;

    public CreateWarehouseUseCase(WarehouseRepository warehouseRepository, WarehouseMapper mapper) {
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public WarehouseResponse execute(CreateWarehouseRequest request) {
        var companyId = TenantContext.requireTenantId();
        String code = Warehouse.normalizeCode(request.code());
        if (warehouseRepository.existsByCompanyIdAndCode(companyId, code)) {
            throw new ConflictException("Warehouse with this code already exists.", java.util.Map.of("code", code));
        }
        Warehouse savedWarehouse;
        try {
            savedWarehouse = warehouseRepository.saveAndFlush(Warehouse.create(companyId, code, request.name()));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Warehouse with this code already exists.", java.util.Map.of("code", code));
        }
        return mapper.toResponse(savedWarehouse);
    }
}
