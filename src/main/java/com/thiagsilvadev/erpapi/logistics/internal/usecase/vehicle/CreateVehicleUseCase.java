package com.thiagsilvadev.erpapi.logistics.internal.usecase.vehicle;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.WRITE;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle.CreateVehicleRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.vehicle.VehicleResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.Vehicle;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;

@Component
public class CreateVehicleUseCase {

    private final VehicleRepository vehicleRepository;
    private final LogisticsMapper mapper;

    public CreateVehicleUseCase(VehicleRepository vehicleRepository, LogisticsMapper mapper) {
        this.vehicleRepository = vehicleRepository;
        this.mapper = mapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public VehicleResponse execute(CreateVehicleRequest request) {
        var companyId = TenantContext.requireTenantId();
        var vehicle = Vehicle.create(companyId, request.plate(), request.description(), request.capacity());
        var plate = vehicle.getLicensePlate();
        if (vehicleRepository.existsByCompanyIdAndPlate(companyId, plate)) {
            throw new ConflictException("Vehicle with this plate already exists.", Map.of("plate", plate.getValue()));
        }
        try {
            return mapper.toResponse(vehicleRepository.saveAndFlush(vehicle));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Vehicle with this plate already exists.", Map.of("plate", plate.getValue()));
        }
    }
}
