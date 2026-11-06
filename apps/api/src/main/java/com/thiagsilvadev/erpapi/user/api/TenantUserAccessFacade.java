package com.thiagsilvadev.erpapi.user.api;

import java.util.Set;
import java.util.UUID;

public interface TenantUserAccessFacade {

    Set<UUID> getActiveUserIds(UUID companyId);

    boolean hasActiveMembership(UUID userId, UUID companyId);

    boolean hasRole(UUID userId, UUID companyId, String roleCode);
}
