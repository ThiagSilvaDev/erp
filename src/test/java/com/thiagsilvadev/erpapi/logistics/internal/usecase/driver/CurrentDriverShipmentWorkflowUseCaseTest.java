package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;
import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentItem;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentTrackingEvent;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.ShipmentReader;

class CurrentDriverShipmentWorkflowUseCaseTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SHIPMENT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-07-29T12:00:00Z");

    private ShipmentRepository shipmentRepository;
    private ShipmentTrackingEventRepository trackingEventRepository;
    private CurrentDriverShipmentWorkflowUseCase useCase;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(COMPANY_ID);
        shipmentRepository = mock(ShipmentRepository.class);
        trackingEventRepository = mock(ShipmentTrackingEventRepository.class);
        useCase = new CurrentDriverShipmentWorkflowUseCase(
                shipmentRepository,
                trackingEventRepository,
                mock(ShipmentReader.class),
                mock(InventoryFacade.class),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldRecordAuthenticatedUserOnAssignedShipmentTrackingEvent() {
        Shipment shipment = shipment();
        when(shipmentRepository.findByIdAndSellerCompanyIdAndDriverUserIdAndDriverStatus(
                        SHIPMENT_ID, COMPANY_ID, USER_ID, LogisticsResourceStatus.ACTIVE))
                .thenReturn(Optional.of(shipment));

        useCase.track(USER_ID, SHIPMENT_ID, new CreateTrackingEventRequest(null, "Warehouse gate", "Ready"));

        ArgumentCaptor<ShipmentTrackingEvent> captor = ArgumentCaptor.forClass(ShipmentTrackingEvent.class);
        verify(trackingEventRepository).save(captor.capture());
        assertThat(captor.getValue().getPerformedByUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getOccurredAt()).isEqualTo(NOW);
        assertThat(captor.getValue().getLocation()).isEqualTo("Warehouse gate");
    }

    @Test
    void shouldHideShipmentNotAssignedToAuthenticatedDriver() {
        when(shipmentRepository.findByIdAndSellerCompanyIdAndDriverUserIdAndDriverStatus(
                        SHIPMENT_ID, COMPANY_ID, USER_ID, LogisticsResourceStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.track(USER_ID, SHIPMENT_ID, null))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(trackingEventRepository, never()).save(any());
    }

    private static Shipment shipment() {
        return Shipment.create(
                1L,
                UUID.randomUUID(),
                COMPANY_ID,
                null,
                UUID.randomUUID(),
                List.of(ShipmentItem.create(UUID.randomUUID(), UUID.randomUUID(), "SKU-1", "Product", 1)));
    }
}
