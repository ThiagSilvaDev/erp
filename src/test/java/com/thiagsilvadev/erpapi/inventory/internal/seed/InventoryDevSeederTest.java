package com.thiagsilvadev.erpapi.inventory.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;
import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovementType;
import com.thiagsilvadev.erpapi.inventory.internal.model.Warehouse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.StockMovementRepository;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;
import com.thiagsilvadev.erpapi.inventory.internal.service.InventoryBalanceService;

class InventoryDevSeederTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-27T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldSeedVariedStockMovementsAcrossDates() {
        WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
        StockMovementRepository movementRepository = mock(StockMovementRepository.class);
        CatalogFacade catalogFacade = mock(CatalogFacade.class);
        CompanyFacade companyFacade = mock(CompanyFacade.class);
        InventoryBalanceService balanceService = mock(InventoryBalanceService.class);
        BusinessNumberService businessNumberService = mock(BusinessNumberService.class);
        Warehouse warehouse = Warehouse.create(COMPANY_ID, "MAIN", "Main");
        ReflectionTestUtils.setField(warehouse, "id", UUID.randomUUID());
        when(companyFacade.resolveCompany("97.986.234/0001-91"))
                .thenReturn(new CompanySnapshot(COMPANY_ID, null, null, null, null, null, null, null, null, null));
        when(warehouseRepository.findByCompanyIdAndCode(COMPANY_ID, "MAIN")).thenReturn(Optional.of(warehouse));
        when(catalogFacade.resolveProduct(any(), any())).thenAnswer(invocation -> {
            String sku = invocation.getArgument(1);
            return new ProductSnapshot(
                    UUID.randomUUID(),
                    sku,
                    "Product " + sku,
                    null,
                    Money.of(new BigDecimal("10.00"), "BRL"),
                    null,
                    null);
        });
        when(businessNumberService.next(any())).thenReturn(1L);
        when(movementRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            StockMovement movement = invocation.getArgument(0);
            ReflectionTestUtils.setField(movement, "id", UUID.randomUUID());
            return movement;
        });

        new InventoryDevSeeder(
                        warehouseRepository,
                        movementRepository,
                        catalogFacade,
                        companyFacade,
                        balanceService,
                        businessNumberService,
                        CLOCK)
                .run(null);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository, times(InventoryDevSeeder.seedMovementCount()))
                .saveAndFlush(captor.capture());
        verify(balanceService, times(InventoryDevSeeder.seedMovementCount()))
                .applyMovement(any(), any(), any(), anyInt(), any());
        verify(movementRepository, times(InventoryDevSeeder.seedMovementCount()))
                .updateCreatedAtForDevSeed(any(), any());
        assertThat(captor.getAllValues())
                .extracting(StockMovement::getType)
                .contains(
                        StockMovementType.IN,
                        StockMovementType.OUT,
                        StockMovementType.RESERVE,
                        StockMovementType.RELEASE_RESERVATION,
                        StockMovementType.ADJUSTMENT,
                        StockMovementType.TRANSFER_IN,
                        StockMovementType.TRANSFER_OUT);
    }
}
