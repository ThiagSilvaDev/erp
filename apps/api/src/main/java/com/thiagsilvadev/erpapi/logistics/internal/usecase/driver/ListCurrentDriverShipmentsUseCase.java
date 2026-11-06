package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.DRIVER_WORKFLOW;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.LogisticsResourceStatus;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment.ShipmentReader;

@Component
public class ListCurrentDriverShipmentsUseCase {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentReader shipmentReader;

    public ListCurrentDriverShipmentsUseCase(ShipmentRepository shipmentRepository, ShipmentReader shipmentReader) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentReader = shipmentReader;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + DRIVER_WORKFLOW + "')")
    public Page<ShipmentResponse> execute(UUID userId, Pageable pageable) {
        UUID companyId = TenantContext.requireTenantId();
        return shipmentRepository
                .findAllBySellerCompanyIdAndDriverUserIdAndDriverStatus(
                        companyId, userId, LogisticsResourceStatus.ACTIVE, pageable)
                .map(shipmentReader::toResponse);
    }
}
