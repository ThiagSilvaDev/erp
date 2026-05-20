package com.thiagsilvadev.erpapi.logistics.internal.dto;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thiagsilvadev.erpapi.logistics.internal.model.*;

@Mapper(componentModel = "spring")
public interface LogisticsMapper {

    DriverResponse toResponse(Driver driver);

    VehicleResponse toResponse(Vehicle vehicle);

    ShipmentItemResponse toResponse(ShipmentItem item);

    ShipmentTrackingEventResponse toResponse(ShipmentTrackingEvent event);

    @Mapping(target = "trackingEvents", source = "trackingEvents")
    ShipmentResponse toResponse(Shipment shipment, List<ShipmentTrackingEvent> trackingEvents);
}
