package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.Set;
import java.util.UUID;

public record TenantRoleCreatedEvent(
        UUID companyId, UUID roleId, String roleCode, Set<String> permissions, UUID actorUserId) {

    public TenantRoleCreatedEvent {
        permissions = Set.copyOf(permissions);
    }
}
