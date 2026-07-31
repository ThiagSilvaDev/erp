package com.thiagsilvadev.erpapi.inventory.internal.dto.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReference;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReferenceProvider;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

class ReservationMapperTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    private final CatalogFacade catalogFacade = org.mockito.Mockito.mock(CatalogFacade.class);
    private final WarehouseRepository warehouseRepository = org.mockito.Mockito.mock(WarehouseRepository.class);
    private final SalesOrderReferenceProvider salesOrderReferenceProvider =
            org.mockito.Mockito.mock(SalesOrderReferenceProvider.class);
    private final ReservationMapper mapper = mapper();

    @Test
    void shouldResolveSalesOrderNumberFromReferenceProvider() {
        UUID salesOrderId = UUID.randomUUID();
        InventoryReservationSnapshot reservation = reservation(salesOrderId);
        when(salesOrderReferenceProvider.getSalesOrderReference(COMPANY_ID, salesOrderId))
                .thenReturn(new SalesOrderReference(salesOrderId, 123L));

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response.salesOrder().id()).isEqualTo(salesOrderId);
        assertThat(response.salesOrder().orderNumber()).isEqualTo(123L);
    }

    @Test
    void shouldRejectMissingSalesOrderReference() {
        UUID salesOrderId = UUID.randomUUID();
        InventoryReservationSnapshot reservation = reservation(salesOrderId);
        when(salesOrderReferenceProvider.getSalesOrderReference(COMPANY_ID, salesOrderId))
                .thenThrow(new ResourceNotFoundException("SalesOrder", salesOrderId));

        assertThatThrownBy(() -> mapper.toResponse(reservation)).isInstanceOf(ResourceNotFoundException.class);
    }

    private ReservationMapper mapper() {
        when(catalogFacade.getProduct(COMPANY_ID, PRODUCT_ID))
                .thenReturn(new com.thiagsilvadev.erpapi.catalog.api.ProductSnapshot(
                        PRODUCT_ID, "SKU", "Product", null, null, null, null));
        ReservationMapper reservationMapper = new ReservationMapperImpl();
        reservationMapper.setCatalogFacade(catalogFacade);
        reservationMapper.setWarehouseRepository(warehouseRepository);
        reservationMapper.setSalesOrderReferenceProvider(salesOrderReferenceProvider);
        return reservationMapper;
    }

    private static InventoryReservationSnapshot reservation(UUID salesOrderId) {
        return new InventoryReservationSnapshot(
                UUID.randomUUID(), COMPANY_ID, PRODUCT_ID, UUID.randomUUID(), 1, salesOrderId);
    }
}
