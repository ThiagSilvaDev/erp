package com.thiagsilvadev.erpapi.user.api;

import java.util.Set;
import java.util.UUID;

public interface TenantAccessProvisioningFacade {

    void provision(UUID userId, UUID companyId, Set<UUID> roleIds);

    void provisionBuiltIn(UUID userId, UUID companyId, String roleCode);
}
