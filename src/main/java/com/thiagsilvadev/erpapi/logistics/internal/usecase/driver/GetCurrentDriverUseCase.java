package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.DRIVER_WORKFLOW;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CurrentDriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.user.api.UserFacade;

@Component
public class GetCurrentDriverUseCase {

    private final DriverRepository driverRepository;
    private final UserFacade userFacade;

    public GetCurrentDriverUseCase(DriverRepository driverRepository, UserFacade userFacade) {
        this.driverRepository = driverRepository;
        this.userFacade = userFacade;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + DRIVER_WORKFLOW + "')")
    public CurrentDriverResponse execute(UUID userId) {
        UUID companyId = TenantContext.requireTenantId();
        Driver driver = driverRepository
                .findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile", userId));
        var user = userFacade.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return new CurrentDriverResponse(
                driver.getId(),
                user.id(),
                user.displayName(),
                user.email(),
                user.phone(),
                driver.getLicenseNumber(),
                driver.getStatus());
    }
}
