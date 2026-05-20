package com.thiagsilvadev.erpapi.logistics.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.logistics.internal.dto.*;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.*;

@RestController
class ShipmentController implements ShipmentApi {

    private final ListShipmentsUseCase listShipmentsUseCase;
    private final GetShipmentByIdUseCase getShipmentByIdUseCase;
    private final AssignDispatchUseCase assignDispatchUseCase;
    private final LoadShipmentUseCase loadShipmentUseCase;
    private final DispatchShipmentUseCase dispatchShipmentUseCase;
    private final AddShipmentTrackingEventUseCase addShipmentTrackingEventUseCase;
    private final DeliverShipmentUseCase deliverShipmentUseCase;
    private final CancelShipmentUseCase cancelShipmentUseCase;

    ShipmentController(
            ListShipmentsUseCase listShipmentsUseCase,
            GetShipmentByIdUseCase getShipmentByIdUseCase,
            AssignDispatchUseCase assignDispatchUseCase,
            LoadShipmentUseCase loadShipmentUseCase,
            DispatchShipmentUseCase dispatchShipmentUseCase,
            AddShipmentTrackingEventUseCase addShipmentTrackingEventUseCase,
            DeliverShipmentUseCase deliverShipmentUseCase,
            CancelShipmentUseCase cancelShipmentUseCase) {
        this.listShipmentsUseCase = listShipmentsUseCase;
        this.getShipmentByIdUseCase = getShipmentByIdUseCase;
        this.assignDispatchUseCase = assignDispatchUseCase;
        this.loadShipmentUseCase = loadShipmentUseCase;
        this.dispatchShipmentUseCase = dispatchShipmentUseCase;
        this.addShipmentTrackingEventUseCase = addShipmentTrackingEventUseCase;
        this.deliverShipmentUseCase = deliverShipmentUseCase;
        this.cancelShipmentUseCase = cancelShipmentUseCase;
    }

    @Override
    public Page<ShipmentResponse> listShipments(ShipmentSearchCriteria criteria, Pageable pageable) {
        return listShipmentsUseCase.execute(criteria, pageable);
    }

    @Override
    public ShipmentResponse getShipment(@PathVariable UUID id) {
        return getShipmentByIdUseCase.execute(id);
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
