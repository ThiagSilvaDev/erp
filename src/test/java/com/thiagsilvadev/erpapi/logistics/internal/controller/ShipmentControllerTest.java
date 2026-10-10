package com.thiagsilvadev.erpapi.logistics.internal.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.thiagsilvadev.erpapi.common.web.exception.DomainExceptionHandler;
import com.thiagsilvadev.erpapi.common.web.exception.ProblemDetailFactory;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentItem;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.*;

class ShipmentControllerTest {

    @Test
    void shouldReturnConflictProblemForInvalidAdministrativeTransition() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = Shipment.create(
                1L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                List.of(ShipmentItem.create(UUID.randomUUID(), UUID.randomUUID(), "SKU-1", "Product", 1)));
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        DispatchShipmentUseCase dispatch = mock(DispatchShipmentUseCase.class);
        when(dispatch.execute(eq(shipmentId), isNull())).thenAnswer(invocation -> {
            shipment.dispatch(clock.instant());
            return null;
        });
        ShipmentController controller = new ShipmentController(
                mock(ListShipmentsUseCase.class),
                mock(GetShipmentByIdUseCase.class),
                mock(AssignDispatchUseCase.class),
                mock(LoadShipmentUseCase.class),
                dispatch,
                mock(AddShipmentTrackingEventUseCase.class),
                mock(DeliverShipmentUseCase.class),
                mock(CancelShipmentUseCase.class));
        MockMvc mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new DomainExceptionHandler(new ProblemDetailFactory(clock)))
                .build();

        mockMvc.perform(post("/api/v1/logistics/shipments/{id}/dispatch", shipmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Invalid shipment status transition from CREATED to IN_TRANSIT"))
                .andExpect(jsonPath("$.instance").value("/api/v1/logistics/shipments/" + shipmentId + "/dispatch"));

        verify(dispatch).execute(shipmentId, null);
    }
}
