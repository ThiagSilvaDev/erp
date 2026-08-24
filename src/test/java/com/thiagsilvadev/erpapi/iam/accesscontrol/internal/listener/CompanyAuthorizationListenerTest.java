package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.company.api.CompanyCreatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.RoleTemplateService;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.TenantAccessProvisioningFacadeImpl;

class CompanyAuthorizationListenerTest {

    private final RoleTemplateService roleTemplateService = mock(RoleTemplateService.class);
    private final TenantAccessProvisioningFacadeImpl membershipService = mock(TenantAccessProvisioningFacadeImpl.class);
    private final CompanyAuthorizationListener listener =
            new CompanyAuthorizationListener(roleTemplateService, membershipService);

    @Test
    void shouldProvisionPlatformCreatorAsInitialOwner() {
        UUID companyId = UUID.randomUUID();
        UUID creatorUserId = UUID.randomUUID();

        listener.on(new CompanyCreatedEvent(companyId, "11222333000181", creatorUserId));

        verify(roleTemplateService).ensureDefaults(companyId);
        verify(membershipService).provisionBuiltIn(creatorUserId, companyId, RoleTemplateService.OWNER);
    }

    @Test
    void shouldOnlyCreateTemplatesWhenCompanyWasSeededWithoutAnAuthenticatedCreator() {
        UUID companyId = UUID.randomUUID();

        listener.on(new CompanyCreatedEvent(companyId, "11222333000181", null));

        verify(roleTemplateService).ensureDefaults(companyId);
        verifyNoInteractions(membershipService);
    }
}
