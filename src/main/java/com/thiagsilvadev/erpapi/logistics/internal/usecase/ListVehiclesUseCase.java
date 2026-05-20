package com.thiagsilvadev.erpapi.logistics.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.dto.VehicleResponse;
import com.thiagsilvadev.erpapi.logistics.internal.repository.VehicleRepository;

@Component
public class ListVehiclesUseCase {

    private final VehicleRepository vehicleRepository;
    private final LogisticsMapper mapper;

    public ListVehiclesUseCase(VehicleRepository vehicleRepository, LogisticsMapper mapper) {
        this.vehicleRepository = vehicleRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions).READ)")
    public Page<VehicleResponse> execute(Pageable pageable) {
        return vehicleRepository.findAll(pageable).map(mapper::toResponse);
    }
}
