package com.thiagsilvadev.erpapi.inventory.internal.dto.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.catalog.api.CatalogFacade;
import com.thiagsilvadev.erpapi.inventory.api.InventoryReservationSnapshot;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReference;
import com.thiagsilvadev.erpapi.inventory.api.SalesOrderReferenceProvider;
import com.thiagsilvadev.erpapi.inventory.internal.repository.WarehouseRepository;

class ReservationMapperTest {

    private final CatalogFacade catalogFacade = org.mockito.Mockito.mock(CatalogFacade.class);
    private final WarehouseRepository warehouseRepository = org.mockito.Mockito.mock(WarehouseRepository.class);
    private final SalesOrderReferenceProvider salesOrderReferenceProvider =
            org.mockito.Mockito.mock(SalesOrderReferenceProvider.class);
    private final ReservationMapper mapper = mapper();

    @Test
    void shouldResolveSalesOrderNumberFromReferenceProvider() {
        UUID salesOrderId = UUID.randomUUID();
        InventoryReservationSnapshot reservation = reservation(salesOrderId);
        when(salesOrderReferenceProvider.findSalesOrderReferenceById(salesOrderId))
                .thenReturn(Optional.of(new SalesOrderReference(salesOrderId, 123L)));

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response.salesOrder().id()).isEqualTo(salesOrderId);
        assertThat(response.salesOrder().orderNumber()).isEqualTo(123L);
    }

    @Test
    void shouldKeepSalesOrderIdWhenReferenceIsMissing() {
        UUID salesOrderId = UUID.randomUUID();
        InventoryReservationSnapshot reservation = reservation(salesOrderId);
        when(salesOrderReferenceProvider.findSalesOrderReferenceById(salesOrderId))
                .thenReturn(Optional.empty());

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response.salesOrder().id()).isEqualTo(salesOrderId);
        assertThat(response.salesOrder().orderNumber()).isNull();
    }

    private ReservationMapper mapper() {
        ReservationMapper reservationMapper = new ReservationMapperImpl();
        reservationMapper.setCatalogFacade(catalogFacade);
        reservationMapper.setWarehouseRepository(warehouseRepository);
        reservationMapper.setSalesOrderReferenceProvider(salesOrderReferenceProvider);
        return reservationMapper;
    }

    private static InventoryReservationSnapshot reservation(UUID salesOrderId) {
        return new InventoryReservationSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1, salesOrderId);
    }
}
