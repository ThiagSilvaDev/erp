package com.thiagsilvadev.erpapi.inventory.internal.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.warehouse.CreateWarehouseRequest;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;
import com.thiagsilvadev.erpapi.inventory.internal.usecase.warehouse.CreateWarehouseUseCase;

class InventoryControllerTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldTranslateDuplicateWarehouseCodeRace() {
        TenantContext.setTenantId(COMPANY_ID);
        WarehouseRepository warehouseRepository = org.mockito.Mockito.mock(WarehouseRepository.class);
        CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(warehouseRepository);
        when(warehouseRepository.existsByCompanyIdAndCode(COMPANY_ID, "MAIN")).thenReturn(false);
        when(warehouseRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Warehouse.class)))
                .thenThrow(new DataIntegrityViolationException("uk_inventory_warehouse_code"));

        assertThatThrownBy(() -> useCase.execute(new CreateWarehouseRequest(" main ", "Main warehouse")))
                .isInstanceOf(ConflictException.class);
    }
}
