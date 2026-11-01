package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.DriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.mapper.LogisticsMapper;
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
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<DriverResponse> execute(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return driverRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
