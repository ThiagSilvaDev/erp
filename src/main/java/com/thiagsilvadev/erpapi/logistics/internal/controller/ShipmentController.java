package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.*;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.*;

@RestController
class ShipmentController implements ShipmentApi {

    private final ShipmentQueryUseCase shipmentQueryUseCase;
    private final AssignDispatchUseCase assignDispatchUseCase;
    private final LoadShipmentUseCase loadShipmentUseCase;
    private final DispatchShipmentUseCase dispatchShipmentUseCase;
    private final AddShipmentTrackingEventUseCase addShipmentTrackingEventUseCase;
    private final DeliverShipmentUseCase deliverShipmentUseCase;
    private final CancelShipmentUseCase cancelShipmentUseCase;

    ShipmentController(
            ShipmentQueryUseCase shipmentQueryUseCase,
            AssignDispatchUseCase assignDispatchUseCase,
            LoadShipmentUseCase loadShipmentUseCase,
            DispatchShipmentUseCase dispatchShipmentUseCase,
            AddShipmentTrackingEventUseCase addShipmentTrackingEventUseCase,
            DeliverShipmentUseCase deliverShipmentUseCase,
            CancelShipmentUseCase cancelShipmentUseCase) {
        this.shipmentQueryUseCase = shipmentQueryUseCase;
        this.assignDispatchUseCase = assignDispatchUseCase;
        this.loadShipmentUseCase = loadShipmentUseCase;
        this.dispatchShipmentUseCase = dispatchShipmentUseCase;
        this.addShipmentTrackingEventUseCase = addShipmentTrackingEventUseCase;
        this.deliverShipmentUseCase = deliverShipmentUseCase;
        this.cancelShipmentUseCase = cancelShipmentUseCase;
    }

    @Override
    public Page<ShipmentResponse> list(ShipmentSearchCriteria criteria, Pageable pageable) {
        return shipmentQueryUseCase.list(criteria, pageable);
    }

    @Override
    public ShipmentResponse getById(@PathVariable UUID id) {
        return shipmentQueryUseCase.getById(id);
    }

    @Override
    public ShipmentResponse assignDispatch(@PathVariable UUID id, @Valid @RequestBody AssignDispatchRequest request) {
        return assignDispatchUseCase.execute(id, request);
    }

    @Override
    public ShipmentResponse load(
            @PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request) {
        return loadShipmentUseCase.execute(id, request);
    }

    @Override
    public ShipmentResponse dispatch(
            @PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request) {
        return dispatchShipmentUseCase.execute(id, request);
    }

    @Override
    public ShipmentResponse addTrackingEvent(
            @PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request) {
        return addShipmentTrackingEventUseCase.execute(id, request);
    }

    @Override
    public ShipmentResponse deliver(
            @PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request) {
        return deliverShipmentUseCase.execute(id, request);
    }

    @Override
    public ShipmentResponse cancel(
            @PathVariable UUID id, @RequestBody(required = false) CreateTrackingEventRequest request) {
        return cancelShipmentUseCase.execute(id, request);
    }
}
