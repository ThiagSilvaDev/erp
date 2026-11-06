package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class AccessControlFacadeImplTest {

    private final TenantAuthorizationService tenantAuthorizationService = mock(TenantAuthorizationService.class);
    private final PlatformAuthorizationService platformAuthorizationService = mock(PlatformAuthorizationService.class);
    private final AccessControlFacadeImpl facade =
            new AccessControlFacadeImpl(tenantAuthorizationService, platformAuthorizationService);

    @Test
    void shouldDelegateActiveMemberships() {
        UUID userId = UUID.randomUUID();

        facade.getActiveMemberships(userId);

        verify(tenantAuthorizationService).getActiveMemberships(userId);
    }

    @Test
    void shouldDelegateActiveMembershipLookup() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        facade.getActiveMembership(userId, companyId);

        verify(tenantAuthorizationService).getActiveMembership(userId, companyId);
    }

    @Test
    void shouldDelegateScopedTokenValidation() {
        UUID membershipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        facade.validateScopedToken(membershipId, userId, companyId, 3L);

        verify(tenantAuthorizationService).validateScopedToken(membershipId, userId, companyId, 3L);
    }

    @Test
    void shouldDelegateActivePlatformRequirement() {
        UUID userId = UUID.randomUUID();

        facade.requireActivePlatform(userId);

        verify(platformAuthorizationService).requireActive(userId);
    }

    @Test
    void shouldDelegatePlatformTokenValidation() {
        UUID assignmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        facade.validatePlatform(assignmentId, userId, 2L);

        verify(platformAuthorizationService).validate(assignmentId, userId, 2L);
    }
}
