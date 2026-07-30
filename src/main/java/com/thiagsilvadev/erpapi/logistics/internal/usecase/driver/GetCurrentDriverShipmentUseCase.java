package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.DRIVER_WORKFLOW;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;
import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.ShipmentReader;

@Component
public class GetCurrentDriverShipmentUseCase {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentReader shipmentReader;

    public GetCurrentDriverShipmentUseCase(ShipmentRepository shipmentRepository, ShipmentReader shipmentReader) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentReader = shipmentReader;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + DRIVER_WORKFLOW + "')")
    public ShipmentResponse execute(UUID userId, UUID shipmentId) {
        UUID companyId = TenantContext.requireTenantId();
        Shipment shipment = shipmentRepository
                .findByIdAndSellerCompanyIdAndDriverUserIdAndDriverStatus(
                        shipmentId, companyId, userId, LogisticsResourceStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(Shipment.class, shipmentId));
        return shipmentReader.toResponse(shipment);
    }
}
