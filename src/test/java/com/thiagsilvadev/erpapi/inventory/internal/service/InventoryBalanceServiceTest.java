package com.thiagsilvadev.erpapi.inventory.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InsufficientInventoryAvailableException;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementType;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

class InventoryBalanceServiceTest {

    private final InventoryItemRepository itemRepository = mock(InventoryItemRepository.class);
    private final WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
    private final CatalogFacade catalogFacade = mock(CatalogFacade.class);

    private final UUID productId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private final Warehouse warehouse = Warehouse.create("MAIN", "Main Warehouse");

    private InventoryBalanceService service;

    @BeforeEach
    void setUp() {
        service = new InventoryBalanceService(itemRepository, warehouseRepository, catalogFacade);
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundInCatalog() {
        UUID unknownProductId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Product", unknownProductId))
                .when(catalogFacade)
                .requireProductExistsById(unknownProductId);

        assertThatThrownBy(() -> service.applyMovement(unknownProductId, warehouseId, 5, StockMovementType.IN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldIncreaseStockForInboundMovement() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        when(itemRepository.findByProductIdAndWarehouseId(productId, warehouseId))
                .thenReturn(Optional.of(item));

        InventoryItem updated = service.applyMovement(productId, warehouseId, 5, StockMovementType.IN);

        assertThat(updated.getQuantityOnHand()).isEqualTo(15);
    }

    @Test
    void shouldDecreaseStockForOutboundMovement() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        when(itemRepository.findByProductIdAndWarehouseId(productId, warehouseId))
                .thenReturn(Optional.of(item));

        InventoryItem updated = service.applyMovement(productId, warehouseId, 4, StockMovementType.OUT);

        assertThat(updated.getQuantityOnHand()).isEqualTo(6);
    }

    @Test
    void shouldNotDecreaseReservedStockWithOutboundMovement() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(7);
        when(itemRepository.findByProductIdAndWarehouseId(productId, warehouseId))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.applyMovement(productId, warehouseId, 4, StockMovementType.OUT))
                .isInstanceOf(InsufficientInventoryAvailableException.class)
                .hasMessage("Insufficient inventory available");
    }

    @Test
    void shouldCreateItemOnlyForInboundMovement() {
        when(itemRepository.findByProductIdAndWarehouseId(productId, warehouseId))
                .thenReturn(Optional.empty());
        when(itemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem created = service.applyMovement(productId, warehouseId, 5, StockMovementType.IN);

        assertThat(created.getQuantityOnHand()).isEqualTo(5);
    }
}
