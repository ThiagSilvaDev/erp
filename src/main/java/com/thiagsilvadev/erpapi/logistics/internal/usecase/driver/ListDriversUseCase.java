package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.DriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;

@Component
public class ListDriversUseCase {

    private final DriverRepository driverRepository;
    private final LogisticsMapper mapper;

    public ListDriversUseCase(DriverRepository driverRepository, LogisticsMapper mapper) {
        this.driverRepository = driverRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions).READ)")
    public Page<DriverResponse> execute(Pageable pageable) {
        return driverRepository.findAll(pageable).map(mapper::toResponse);
    }
}
