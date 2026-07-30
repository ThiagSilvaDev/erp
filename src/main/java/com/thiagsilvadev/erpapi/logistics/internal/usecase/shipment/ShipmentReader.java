package com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentTrackingEventRepository;

@Component
public class ShipmentReader {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingEventRepository trackingEventRepository;
    private final LogisticsMapper mapper;

    ShipmentReader(
            ShipmentRepository shipmentRepository,
            ShipmentTrackingEventRepository trackingEventRepository,
            LogisticsMapper mapper) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.mapper = mapper;
    }

    Shipment get(UUID id) {
        var sellerCompanyId = TenantContext.requireTenantId();
        return shipmentRepository
                .findByIdAndSellerCompanyId(id, sellerCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException(Shipment.class, id));
    }

    public ShipmentResponse toResponse(Shipment shipment) {
        return mapper.toResponse(
                shipment, trackingEventRepository.findByShipmentIdOrderByOccurredAtAsc(shipment.getId()));
    }
}
