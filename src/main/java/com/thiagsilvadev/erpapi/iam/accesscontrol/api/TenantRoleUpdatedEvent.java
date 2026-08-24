package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.Set;
import java.util.UUID;

public record TenantRoleUpdatedEvent(
        UUID companyId,
        UUID roleId,
        String roleCode,
        Set<String> beforePermissions,
        Set<String> afterPermissions,
        UUID actorUserId) {

    public TenantRoleUpdatedEvent {
        beforePermissions = Set.copyOf(beforePermissions);
        afterPermissions = Set.copyOf(afterPermissions);
    }
}
