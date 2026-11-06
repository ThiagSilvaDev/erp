package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;

@Service
public class AccessControlFacadeImpl implements AccessControlFacade {

    private final TenantAuthorizationService tenantAuthorizationService;
    private final PlatformAuthorizationService platformAuthorizationService;

    public AccessControlFacadeImpl(
            TenantAuthorizationService tenantAuthorizationService,
            PlatformAuthorizationService platformAuthorizationService) {
        this.tenantAuthorizationService = tenantAuthorizationService;
        this.platformAuthorizationService = platformAuthorizationService;
    }

    @Override
    public List<TenantAuthorizationSnapshot> getActiveMemberships(UUID userId) {
        return tenantAuthorizationService.getActiveMemberships(userId);
    }

    @Override
    public TenantAuthorizationSnapshot getActiveMembership(UUID userId, UUID companyId) {
        return tenantAuthorizationService.getActiveMembership(userId, companyId);
    }

    @Override
    public TenantAuthorizationSnapshot validateScopedToken(
            UUID membershipId, UUID userId, UUID companyId, long authorizationVersion) {
        return tenantAuthorizationService.validateScopedToken(membershipId, userId, companyId, authorizationVersion);
    }

    @Override
    public PlatformAuthorizationSnapshot requireActivePlatform(UUID userId) {
        return platformAuthorizationService.requireActive(userId);
    }

    @Override
    public PlatformAuthorizationSnapshot validatePlatform(UUID assignmentId, UUID userId, long authorizationVersion) {
        return platformAuthorizationService.validate(assignmentId, userId, authorizationVersion);
    }
}
