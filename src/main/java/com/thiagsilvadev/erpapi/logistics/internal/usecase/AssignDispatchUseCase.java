package com.thiagsilvadev.erpapi.logistics.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.logistics.internal.dto.AssignDispatchRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.*;
import com.thiagsilvadev.erpapi.logistics.internal.repository.*;

@Component
public class AssignDispatchUseCase {

    private final ShipmentReader shipmentReader;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentTrackingEventRepository trackingEventRepository;

    public AssignDispatchUseCase(
            ShipmentReader shipmentReader,
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            ShipmentTrackingEventRepository trackingEventRepository) {
        this.shipmentReader = shipmentReader;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions).DISPATCH)")
    public ShipmentResponse execute(UUID shipmentId, AssignDispatchRequest request) {
        Shipment shipment = shipmentReader.get(shipmentId);
        Driver driver = driverRepository
                .findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException(Driver.class, request.driverId()));
        Vehicle vehicle = vehicleRepository
                .findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException(Vehicle.class, request.vehicleId()));
        shipment.assignDispatch(
                driver, vehicle, request.routeName(), request.originWarehouseId(), request.destinationDescription());
        trackingEventRepository.save(ShipmentTrackingEvent.create(
                shipment, ShipmentStatus.DISPATCH_ASSIGNED, java.time.Instant.now(), null, "Dispatch assigned"));
        return shipmentReader.toResponse(shipment);
    }
}
