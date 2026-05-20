package com.thiagsilvadev.erpapi.logistics.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.logistics.internal.dto.DriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;

@Component
public class GetDriverByIdUseCase {

    private final DriverRepository driverRepository;
    private final LogisticsMapper mapper;

    public GetDriverByIdUseCase(DriverRepository driverRepository, LogisticsMapper mapper) {
        this.driverRepository = driverRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions).READ)")
    public DriverResponse execute(UUID id) {
        return driverRepository
                .findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(Driver.class, id));
    }
}
