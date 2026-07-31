package com.thiagsilvadev.erpapi.logistics.internal.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentStatus;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;
import com.thiagsilvadev.erpapi.sales.api.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderLogisticsListenerTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentTrackingEventRepository trackingEventRepository;

    @Mock
    private BusinessNumberService businessNumberService;

    @Mock
    private SalesFacade salesFacade;

    @Test
    void shouldCreateShipmentWhenSalesOrderDeliveryIsRequested() {
        SalesOrderDeliveryRequestedEvent event = event();
        when(shipmentRepository.existsBySalesOrderId(event.salesOrderId())).thenReturn(false);
        when(salesFacade.getOrder(event.sellerCompanyId(), event.salesOrderId()))
                .thenReturn(order(event.salesOrderId(), event.sellerCompanyId()));
        when(businessNumberService.next(BusinessNumberType.SHIPMENT)).thenReturn(500L);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        listener().on(event);

        ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(shipmentCaptor.capture());
        Shipment shipment = shipmentCaptor.getValue();
        assertThat(shipment.getShipmentNumber()).isEqualTo(500L);
        assertThat(shipment.getSalesOrderId()).isEqualTo(event.salesOrderId());
        assertThat(shipment.getItems()).hasSize(1);
        assertThat(shipment.getRouteName()).isEqualTo("Route A");
        verify(trackingEventRepository).save(any());
    }

    @Test
    void shouldIgnoreDuplicateDeliveryRequestedEvent() {
        SalesOrderDeliveryRequestedEvent event = event();
        when(shipmentRepository.existsBySalesOrderId(event.salesOrderId())).thenReturn(true);

        listener().on(event);

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void shouldCancelShipmentBeforeLoadingWhenSalesOrderIsCancelled() {
        UUID salesOrderId = UUID.randomUUID();
        Shipment shipment = Shipment.create(
                1L,
                salesOrderId,
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                List.of(com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentItem.create(
                        UUID.randomUUID(), UUID.randomUUID(), "SKU-1", "Product", 1)));
        when(shipmentRepository.findBySalesOrderId(salesOrderId)).thenReturn(Optional.of(shipment));

        listener().on(new SalesOrderCancelledEvent(salesOrderId));

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        verify(trackingEventRepository).save(any());
    }

    private SalesOrderLogisticsListener listener() {
        return new SalesOrderLogisticsListener(
                shipmentRepository, trackingEventRepository, businessNumberService, clock, salesFacade);
    }

    private SalesOrderDeliveryRequestedEvent event() {
        return new SalesOrderDeliveryRequestedEvent(UUID.randomUUID(), UUID.randomUUID());
    }

    private static SalesOrderSnapshot order(UUID id, UUID sellerCompanyId) {
        return new SalesOrderSnapshot(
                id,
                100L,
                SaleType.B2C,
                FulfillmentType.DELIVERY,
                PaymentMethod.PIX,
                new SalesOrderDeliveryRouteSnapshot("Route A", UUID.randomUUID(), "Customer HQ"),
                sellerCompanyId,
                null,
                UUID.randomUUID(),
                OrderStatus.CONFIRMED,
                java.math.BigDecimal.TEN,
                "BRL",
                java.util.Set.of(
                        new SalesOrderItemSnapshot(UUID.randomUUID(), UUID.randomUUID(), "SKU-1", "Product", 2)));
    }
}
