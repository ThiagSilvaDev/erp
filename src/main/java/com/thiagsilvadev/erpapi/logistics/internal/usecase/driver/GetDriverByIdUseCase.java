package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.DriverResponse;
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
    @PreAuthorize("hasAuthority('" + READ + "')")
    public DriverResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return driverRepository
                .findByIdAndCompanyId(id, companyId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(Driver.class, id));
    }
}
