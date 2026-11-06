package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentItem;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.ShipmentReader;

class GetCurrentDriverShipmentUseCaseTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SHIPMENT_ID = UUID.randomUUID();

    private ShipmentRepository shipmentRepository;
    private ShipmentReader shipmentReader;
    private GetCurrentDriverShipmentUseCase useCase;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(COMPANY_ID);
        shipmentRepository = mock(ShipmentRepository.class);
        shipmentReader = mock(ShipmentReader.class);
        useCase = new GetCurrentDriverShipmentUseCase(shipmentRepository, shipmentReader);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldReturnAssignedShipment() {
        Shipment shipment = shipment();
        ShipmentResponse response = mock(ShipmentResponse.class);
        when(shipmentRepository.findByIdAndSellerCompanyIdAndDriverUserIdAndDriverStatus(
                        SHIPMENT_ID, COMPANY_ID, USER_ID, LogisticsResourceStatus.ACTIVE))
                .thenReturn(Optional.of(shipment));
        when(shipmentReader.toResponse(shipment)).thenReturn(response);

        ShipmentResponse result = useCase.execute(USER_ID, SHIPMENT_ID);

        assertThat(result).isSameAs(response);
    }

    @Test
    void shouldHideShipmentAssignedToAnotherDriver() {
        assertShipmentNotFound();
    }

    @Test
    void shouldHideShipmentFromAnotherCompany() {
        assertShipmentNotFound();
    }

    @Test
    void shouldHideMissingShipment() {
        assertShipmentNotFound();
    }

    @Test
    void shouldRejectMissingDriverBinding() {
        assertShipmentNotFound();
    }

    @Test
    void shouldRejectInactiveDriverBinding() {
        assertShipmentNotFound();
    }

    private void assertShipmentNotFound() {
        when(shipmentRepository.findByIdAndSellerCompanyIdAndDriverUserIdAndDriverStatus(
                        SHIPMENT_ID, COMPANY_ID, USER_ID, LogisticsResourceStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_ID, SHIPMENT_ID)).isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(shipmentReader);
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
