package com.thiagsilvadev.erpapi.inventory.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InsufficientInventoryAvailableException;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InsufficientReservedInventoryException;
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
    private final UUID companyId = UUID.randomUUID();
    private final Warehouse warehouse = Warehouse.create(companyId, "MAIN", "Main Warehouse");

    private InventoryBalanceService service;

    @BeforeEach
    void setUp() {
        service = new InventoryBalanceService(itemRepository, warehouseRepository, catalogFacade);
        when(warehouseRepository.findByIdAndCompanyId(warehouseId, companyId)).thenReturn(Optional.of(warehouse));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundInCatalog() {
        UUID unknownProductId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Product", unknownProductId))
                .when(catalogFacade)
                .getProduct(companyId, unknownProductId);

        assertThatThrownBy(
                        () -> service.applyMovement(companyId, unknownProductId, warehouseId, 5, StockMovementType.IN))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(warehouseRepository, never()).findByIdAndCompanyId(warehouseId, companyId);
    }

    @Test
    void shouldIncreaseStockForInboundMovement() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        when(itemRepository.findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId))
                .thenReturn(Optional.of(item));

        InventoryItem updated = service.applyMovement(companyId, productId, warehouseId, 5, StockMovementType.IN);

        assertThat(updated.getQuantityOnHand()).isEqualTo(15);
    }

    @Test
    void shouldDecreaseStockForOutboundMovement() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        when(itemRepository.findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId))
                .thenReturn(Optional.of(item));

        InventoryItem updated = service.applyMovement(companyId, productId, warehouseId, 4, StockMovementType.OUT);

        assertThat(updated.getQuantityOnHand()).isEqualTo(6);
    }

    @ParameterizedTest
    @MethodSource("movementScenarios")
    void shouldApplyMovementOperation(
            StockMovementType type, int expectedOnHand, int expectedReserved, int expectedAvailable) {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        if (type == StockMovementType.RELEASE_RESERVATION) {
            item.reserve(4);
        }
        when(itemRepository.findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId))
                .thenReturn(Optional.of(item));

        InventoryItem updated = service.applyMovement(companyId, productId, warehouseId, 2, type);

        assertThat(updated.getQuantityOnHand()).isEqualTo(expectedOnHand);
        assertThat(updated.getReservedQuantity()).isEqualTo(expectedReserved);
        assertThat(updated.availableQuantity()).isEqualTo(expectedAvailable);
    }

    private static Stream<Arguments> movementScenarios() {
        return Stream.of(
                arguments(StockMovementType.IN, 12, 0, 12),
                arguments(StockMovementType.OUT, 8, 0, 8),
                arguments(StockMovementType.RESERVE, 10, 2, 8),
                arguments(StockMovementType.RELEASE_RESERVATION, 10, 2, 8),
                arguments(StockMovementType.TRANSFER_IN, 12, 0, 12),
                arguments(StockMovementType.TRANSFER_OUT, 8, 0, 8),
                arguments(StockMovementType.ADJUSTMENT, 12, 0, 12));
    }

    @ParameterizedTest
    @EnumSource(
            value = StockMovementType.class,
            names = {"IN", "TRANSFER_IN", "ADJUSTMENT"})
    void shouldCreateItemForInboundMovements(StockMovementType type) {
        when(itemRepository.findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId))
                .thenReturn(Optional.empty());
        when(itemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem created = service.applyMovement(companyId, productId, warehouseId, 5, type);

        assertThat(created.getQuantityOnHand()).isEqualTo(5);
        assertThat(created.getReservedQuantity()).isZero();
        assertThat(created.availableQuantity()).isEqualTo(5);
        verify(itemRepository).save(any(InventoryItem.class));
    }

    @ParameterizedTest
    @EnumSource(
            value = StockMovementType.class,
            names = {"OUT", "RESERVE", "RELEASE_RESERVATION", "TRANSFER_OUT"})
    void shouldRejectMovementWhenInventoryItemDoesNotExist(StockMovementType type) {
        when(itemRepository.findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyMovement(companyId, productId, warehouseId, 2, type))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(itemRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void shouldNotDecreaseReservedStockWithOutboundMovement() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        item.reserve(7);
        when(itemRepository.findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.applyMovement(companyId, productId, warehouseId, 4, StockMovementType.OUT))
                .isInstanceOf(InsufficientInventoryAvailableException.class)
                .hasMessage("Insufficient inventory available");
    }

    @Test
    void shouldRejectReleaseWhenNoQuantityIsReserved() {
        InventoryItem item = InventoryItem.create(productId, warehouse, 10);
        when(itemRepository.findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.applyMovement(
                        companyId, productId, warehouseId, 1, StockMovementType.RELEASE_RESERVATION))
                .isInstanceOf(InsufficientReservedInventoryException.class);
    }

    @Test
    void shouldRejectWarehouseOutsideCompany() {
        when(warehouseRepository.findByIdAndCompanyId(warehouseId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyMovement(companyId, productId, warehouseId, 1, StockMovementType.IN))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(itemRepository, never()).findByCompanyIdAndProductIdAndWarehouseId(companyId, productId, warehouseId);
    }
}
