package com.thiagsilvadev.erpapi.logistics.internal.usecase.vehicle;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle.VehicleResponse;
import com.thiagsilvadev.erpapi.logistics.internal.mapper.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;

@Component
public class GetVehicleByIdUseCase {

    private final VehicleRepository vehicleRepository;
    private final LogisticsMapper mapper;

    public GetVehicleByIdUseCase(VehicleRepository vehicleRepository, LogisticsMapper mapper) {
        this.vehicleRepository = vehicleRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public VehicleResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return vehicleRepository
                .findByIdAndCompanyId(id, companyId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(Vehicle.class, id));
    }
}
