package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.Set;
import java.util.UUID;

public record TenantMembershipRolesReplacedEvent(
        UUID companyId, UUID membershipId, Set<UUID> beforeRoleIds, Set<UUID> afterRoleIds, UUID actorUserId) {

    public TenantMembershipRolesReplacedEvent {
        beforeRoleIds = Set.copyOf(beforeRoleIds);
        afterRoleIds = Set.copyOf(afterRoleIds);
    }
}
