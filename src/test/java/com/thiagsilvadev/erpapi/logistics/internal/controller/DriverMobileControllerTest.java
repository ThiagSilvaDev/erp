package com.thiagsilvadev.erpapi.logistics.internal.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentStatus;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.CurrentDriverShipmentWorkflowUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.GetCurrentDriverShipmentUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.GetCurrentDriverUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.ListCurrentDriverShipmentsUseCase;

class DriverMobileControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private GetCurrentDriverShipmentUseCase getCurrentDriverShipmentUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        getCurrentDriverShipmentUseCase = mock(GetCurrentDriverShipmentUseCase.class);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(USER_ID.toString())
                .build();
        DriverMobileController controller = new DriverMobileController(
                mock(GetCurrentDriverUseCase.class),
                mock(ListCurrentDriverShipmentsUseCase.class),
                getCurrentDriverShipmentUseCase,
                mock(CurrentDriverShipmentWorkflowUseCase.class));
        mockMvc = standaloneSetup(controller)
                .setCustomArgumentResolvers(authenticationPrincipal(jwt))
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
