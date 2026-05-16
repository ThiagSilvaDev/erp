package com.thiagsilvadev.erpapi.inventory.internal.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.CreateWarehouseRequest;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryReservationRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

class InventoryControllerTest {

    @Test
    void shouldTranslateDuplicateWarehouseCodeRace() {
        WarehouseRepository warehouseRepository = org.mockito.Mockito.mock(WarehouseRepository.class);
        InventoryController controller = new InventoryController(
                warehouseRepository,
                org.mockito.Mockito.mock(InventoryItemRepository.class),
                org.mockito.Mockito.mock(StockMovementRepository.class),
                org.mockito.Mockito.mock(InventoryReservationRepository.class),
                org.mockito.Mockito.mock(InventoryService.class),
                org.mockito.Mockito.mock(InventoryMapper.class));
        when(warehouseRepository.existsByCode("MAIN")).thenReturn(false);
        when(warehouseRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Warehouse.class)))
                .thenThrow(new DataIntegrityViolationException("uk_inventory_warehouse_code"));

        assertThatThrownBy(() -> controller.createWarehouse(new CreateWarehouseRequest(" main ", "Main warehouse")))
                .isInstanceOf(ConflictException.class);
    }
}
