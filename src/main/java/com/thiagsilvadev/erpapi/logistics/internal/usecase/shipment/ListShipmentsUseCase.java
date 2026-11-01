package com.thiagsilvadev.erpapi.logistics.internal.usecase.shipment;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentResponse;
import com.thiagsilvadev.erpapi.logistics.internal.dto.shipment.ShipmentSearchCriteria;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentRepository;
import com.thiagsilvadev.erpapi.logistics.internal.repository.ShipmentSpecification;

@Component
public class ListShipmentsUseCase {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentReader shipmentReader;

    public ListShipmentsUseCase(ShipmentRepository shipmentRepository, ShipmentReader shipmentReader) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentReader = shipmentReader;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<ShipmentResponse> execute(ShipmentSearchCriteria criteria, Pageable pageable) {
        var sellerCompanyId = TenantContext.requireTenantId();
        return shipmentRepository
                .findAll(ShipmentSpecification.withCriteriaForSeller(criteria, sellerCompanyId), pageable)
                .map(shipmentReader::toResponse);
    }
}
