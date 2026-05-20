package com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;

@Component
public class GetShipmentByIdUseCase {

    private final ShipmentReader shipmentReader;

    public GetShipmentByIdUseCase(ShipmentReader shipmentReader) {
        this.shipmentReader = shipmentReader;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions).READ)")
    public ShipmentResponse execute(UUID id) {
        return shipmentReader.toResponse(shipmentReader.get(id));
    }
}
