package com.thiagsilvadev.erpapi.logistics.internal.usecase.driver;

import static com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions.WRITE;

import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.logistics.internal.dto.LogisticsMapper;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.CreateDriverRequest;
import com.thiagsilvadev.erpapi.logistics.internal.dto.driver.DriverResponse;
import com.thiagsilvadev.erpapi.logistics.internal.model.Driver;
import com.thiagsilvadev.erpapi.logistics.internal.repository.DriverRepository;
import com.thiagsilvadev.erpapi.user.api.TenantUserAccessFacade;
import com.thiagsilvadev.erpapi.user.api.UserFacade;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserStatus;

@Component
public class CreateDriverUseCase {

    private final DriverRepository driverRepository;
    private final LogisticsMapper mapper;
    private final UserFacade userFacade;
    private final TenantUserAccessFacade tenantUserAccessFacade;

    public CreateDriverUseCase(
            DriverRepository driverRepository,
            LogisticsMapper mapper,
            UserFacade userFacade,
            TenantUserAccessFacade tenantUserAccessFacade) {
        this.driverRepository = driverRepository;
        this.mapper = mapper;
        this.userFacade = userFacade;
        this.tenantUserAccessFacade = tenantUserAccessFacade;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public DriverResponse execute(CreateDriverRequest request) {
        var companyId = TenantContext.requireTenantId();
        var licenseNumber = Driver.normalizeLicenseNumber(request.licenseNumber());

        var user = userFacade.getUser(request.userId());

        validateActiveDriverMembership(user, companyId);
        validateDriverDoesNotExist(companyId, request.userId(), licenseNumber);

        return persistDriver(companyId, request.userId(), licenseNumber);
    }

    private void validateActiveDriverMembership(UserSnapshot user, UUID companyId) {
        if (user.status() != UserStatus.ACTIVE) {
            throw new ConflictException("User must be active.", Map.of("userId", user.id()));
        }

        if (!tenantUserAccessFacade.hasRole(user.id(), companyId, ModulePermissionProvider.ROLE_DRIVER)) {
            throw new ConflictException(
                    "User must have an active DRIVER membership in the current company.", Map.of("userId", user.id()));
        }
    }

    private void validateDriverDoesNotExist(UUID companyId, UUID userId, String licenseNumber) {
        if (driverRepository.existsByCompanyIdAndUserId(companyId, userId)) {
            throw new ConflictException("Driver profile already exists for this user.", Map.of("userId", userId));
        }

        if (driverRepository.existsByCompanyIdAndLicenseNumber(companyId, licenseNumber)) {
            throw new ConflictException(
                    "Driver with this license number already exists.", Map.of("licenseNumber", licenseNumber));
        }
    }

    private DriverResponse persistDriver(UUID companyId, UUID userId, String licenseNumber) {
        try {
            var driver = driverRepository.saveAndFlush(Driver.create(companyId, userId, licenseNumber));
            return mapper.toResponse(driver);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Driver profile already exists for this user or license number.",
                    Map.of("userId", userId, "licenseNumber", licenseNumber));
        }
    }
}
