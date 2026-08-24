package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.Set;
import java.util.UUID;

public record TenantRoleDeletedEvent(
        UUID companyId, UUID roleId, String roleCode, Set<String> permissions, UUID actorUserId) {

    public TenantRoleDeletedEvent {
        permissions = Set.copyOf(permissions);
    }
}
