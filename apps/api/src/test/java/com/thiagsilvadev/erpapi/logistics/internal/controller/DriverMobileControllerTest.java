package com.thiagsilvadev.erpapi.logistics.internal.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.common.web.exception.DomainExceptionHandler;
import com.thiagsilvadev.erpapi.common.web.exception.ProblemDetailFactory;
import com.thiagsilvadev.erpapi.inventory.api.InventoryFacade;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentItem;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentStatus;
import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.CurrentDriverShipmentWorkflowUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.GetCurrentDriverShipmentUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.GetCurrentDriverUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.ListCurrentDriverShipmentsUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.ShipmentReader;

class DriverMobileControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private GetCurrentDriverShipmentUseCase getCurrentDriverShipmentUseCase;
    private MockMvc mockMvc;
    private ShipmentRepository shipmentRepository;
    private ShipmentTrackingEventRepository trackingEventRepository;

    @BeforeEach
    void setUp() {
        getCurrentDriverShipmentUseCase = mock(GetCurrentDriverShipmentUseCase.class);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(USER_ID.toString())
                .build();
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        shipmentRepository = mock(ShipmentRepository.class);
        trackingEventRepository = mock(ShipmentTrackingEventRepository.class);
        CurrentDriverShipmentWorkflowUseCase workflow = new CurrentDriverShipmentWorkflowUseCase(
                shipmentRepository,
                trackingEventRepository,
                mock(ShipmentReader.class),
                mock(InventoryFacade.class),
                clock);
        DriverMobileController controller = new DriverMobileController(
                mock(GetCurrentDriverUseCase.class),
                mock(ListCurrentDriverShipmentsUseCase.class),
                getCurrentDriverShipmentUseCase,
                workflow);
        mockMvc = standaloneSetup(controller)
                .setCustomArgumentResolvers(authenticationPrincipal(jwt))
                .setControllerAdvice(new DomainExceptionHandler(new ProblemDetailFactory(clock)))
                .build();
    }

    @Test
    void shouldGetCurrentDriverShipment() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        when(getCurrentDriverShipmentUseCase.execute(USER_ID, shipmentId)).thenReturn(shipmentResponse(shipmentId));

        mockMvc.perform(get("/api/v1/logistics/drivers/me/shipments/{id}", shipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shipmentId.toString()));

        verify(getCurrentDriverShipmentUseCase).execute(USER_ID, shipmentId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldReturnConflictProblemForInvalidDriverTransition() throws Exception {
        UUID companyId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        Shipment shipment = Shipment.create(
                1L,
                UUID.randomUUID(),
                companyId,
                null,
                UUID.randomUUID(),
                List.of(ShipmentItem.create(UUID.randomUUID(), UUID.randomUUID(), "SKU-1", "Product", 1)));
        shipment.assignDispatch(
                Driver.create(companyId, USER_ID, "CNH-1"),
                Vehicle.create(companyId, "ABC1D23", "Truck", 1000),
                "Downtown",
                UUID.randomUUID(),
                "Customer dock");
        when(shipmentRepository.findByIdAndSellerCompanyIdAndDriverUserIdAndDriverStatus(
                        shipmentId, companyId, USER_ID, LogisticsResourceStatus.ACTIVE))
                .thenReturn(Optional.of(shipment));

        mockMvc.perform(post("/api/v1/logistics/drivers/me/shipments/{id}/deliver", shipmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid shipment status transition from DISPATCH_ASSIGNED to DELIVERED"))
                .andExpect(jsonPath("$.instance")
                        .value("/api/v1/logistics/drivers/me/shipments/" + shipmentId + "/deliver"));

        verify(trackingEventRepository, never()).save(any());
    }

    private static HandlerMethodArgumentResolver authenticationPrincipal(Jwt jwt) {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory) {
                return jwt;
            }
        };
    }

    private static ShipmentResponse shipmentResponse(UUID id) {
        return new ShipmentResponse(
                id,
                1L,
                "SHP-000001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                ShipmentStatus.CREATED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of());
    }
}
