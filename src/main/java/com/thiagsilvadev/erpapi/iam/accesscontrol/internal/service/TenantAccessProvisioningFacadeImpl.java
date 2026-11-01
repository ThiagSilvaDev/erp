package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.thiagsilvadev.erpapi.user.api.TenantAccessProvisioningFacade;

/**
 * Cross-module adapter for the {@link TenantAccessProvisioningFacade} contract. The membership
 * workflow itself lives in {@link TenantMembershipService} so that use cases depend on the service
 * instead of on a facade implementation.
 */
@Service
public class TenantAccessProvisioningFacadeImpl implements TenantAccessProvisioningFacade {

    private final TenantMembershipService membershipService;

    public TenantAccessProvisioningFacadeImpl(TenantMembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @Override
    public void provision(UUID userId, UUID companyId, Set<UUID> roleIds) {
        membershipService.provision(userId, companyId, roleIds);
    }

    @Override
    public void provisionBuiltIn(UUID userId, UUID companyId, String roleCode) {
        membershipService.provisionBuiltIn(userId, companyId, roleCode);
    }
}
