package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CurrentDriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.CreateTrackingEventRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.CurrentDriverShipmentWorkflowUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.GetCurrentDriverShipmentUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.GetCurrentDriverUseCase;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.driver.ListCurrentDriverShipmentsUseCase;

@RestController
class DriverMobileController implements DriverMobileApi {

    private final GetCurrentDriverUseCase getCurrentDriverUseCase;
    private final ListCurrentDriverShipmentsUseCase listCurrentDriverShipmentsUseCase;
    private final GetCurrentDriverShipmentUseCase getCurrentDriverShipmentUseCase;
    private final CurrentDriverShipmentWorkflowUseCase workflowUseCase;

    DriverMobileController(
            GetCurrentDriverUseCase getCurrentDriverUseCase,
            ListCurrentDriverShipmentsUseCase listCurrentDriverShipmentsUseCase,
            GetCurrentDriverShipmentUseCase getCurrentDriverShipmentUseCase,
            CurrentDriverShipmentWorkflowUseCase workflowUseCase) {
        this.getCurrentDriverUseCase = getCurrentDriverUseCase;
        this.listCurrentDriverShipmentsUseCase = listCurrentDriverShipmentsUseCase;
        this.getCurrentDriverShipmentUseCase = getCurrentDriverShipmentUseCase;
        this.workflowUseCase = workflowUseCase;
    }

    @Override
    public CurrentDriverResponse getCurrentDriver(Jwt jwt) {
        return getCurrentDriverUseCase.execute(userId(jwt));
    }

    @Override
    public Page<ShipmentResponse> listShipments(Jwt jwt, Pageable pageable) {
        return listCurrentDriverShipmentsUseCase.execute(userId(jwt), pageable);
    }

    @Override
    public ShipmentResponse getShipment(Jwt jwt, UUID id) {
        return getCurrentDriverShipmentUseCase.execute(userId(jwt), id);
    }

    @Override
    public ShipmentResponse load(Jwt jwt, UUID id, CreateTrackingEventRequest request) {
        return workflowUseCase.load(userId(jwt), id, request);
    }

    @Override
    public ShipmentResponse dispatch(Jwt jwt, UUID id, CreateTrackingEventRequest request) {
        return workflowUseCase.dispatch(userId(jwt), id, request);
    }

    @Override
    public ShipmentResponse addTrackingEvent(Jwt jwt, UUID id, CreateTrackingEventRequest request) {
        return workflowUseCase.track(userId(jwt), id, request);
    }

    @Override
    public ShipmentResponse deliver(Jwt jwt, UUID id, CreateTrackingEventRequest request) {
        return workflowUseCase.deliver(userId(jwt), id, request);
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
