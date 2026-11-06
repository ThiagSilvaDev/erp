package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.List;
import java.util.UUID;

public interface AccessControlFacade {

    List<TenantAuthorizationSnapshot> getActiveMemberships(UUID userId);

    TenantAuthorizationSnapshot getActiveMembership(UUID userId, UUID companyId);

    TenantAuthorizationSnapshot validateScopedToken(
            UUID membershipId, UUID userId, UUID companyId, long authorizationVersion);

    PlatformAuthorizationSnapshot requireActivePlatform(UUID userId);

    PlatformAuthorizationSnapshot validatePlatform(UUID assignmentId, UUID userId, long authorizationVersion);
}
