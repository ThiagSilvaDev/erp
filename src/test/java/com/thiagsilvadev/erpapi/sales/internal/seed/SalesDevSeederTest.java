package com.thiagsilvadev.erpapi.sales.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
import com.thiagsilvadev.erpapi.customer.api.CustomerFacade;
import com.thiagsilvadev.erpapi.customer.api.CustomerSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.inventory.api.WarehouseSnapshot;
import com.thiagsilvadev.erpapi.sales.api.SalesOrderStatus;
import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;
import com.thiagsilvadev.erpapi.sales.internal.repository.SalesOrderRepository;

class SalesDevSeederTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-27T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldSeedOrdersAcrossStatusesAndDates() {
        SalesOrderRepository repository = mock(SalesOrderRepository.class);
        CustomerFacade customerFacade = mock(CustomerFacade.class);
        CompanyFacade companyFacade = mock(CompanyFacade.class);
        CatalogFacade catalogFacade = mock(CatalogFacade.class);
        InventoryFacade inventoryFacade = mock(InventoryFacade.class);
        BusinessNumberService businessNumberService = mock(BusinessNumberService.class);
        when(companyFacade.resolveCompany("97.986.234/0001-91"))
                .thenReturn(new CompanySnapshot(COMPANY_ID, null, null, null, null, null, null, null, null, null));
        when(customerFacade.resolveCustomer(COMPANY_ID, "12345678901"))
                .thenReturn(new CustomerSnapshot(UUID.randomUUID(), "Customer", "12345678901", COMPANY_ID));
        when(inventoryFacade.resolveWarehouse(COMPANY_ID, "MAIN"))
                .thenReturn(new WarehouseSnapshot(UUID.randomUUID(), "MAIN", "Main"));
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
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            SalesOrder order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
            return order;
        });

        new SalesDevSeeder(
                        repository,
                        customerFacade,
                        companyFacade,
                        catalogFacade,
                        inventoryFacade,
                        businessNumberService,
                        CLOCK)
                .run(null);

        ArgumentCaptor<SalesOrder> captor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(repository, times(SalesDevSeeder.seedOrderCount())).saveAndFlush(captor.capture());
        verify(repository, times(SalesDevSeeder.seedOrderCount())).updateCreatedAtForDevSeed(any(), any());
        assertThat(captor.getAllValues())
                .extracting(SalesOrder::getStatus)
                .contains(SalesOrderStatus.DRAFT, SalesOrderStatus.CONFIRMED, SalesOrderStatus.CANCELLED);
    }
}
