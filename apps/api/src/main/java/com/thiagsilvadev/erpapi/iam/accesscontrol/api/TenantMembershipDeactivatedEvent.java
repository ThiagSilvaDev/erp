package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.Set;
import java.util.UUID;

public record TenantMembershipDeactivatedEvent(
        UUID companyId, UUID membershipId, UUID userId, Set<UUID> roleIds, UUID actorUserId) {

    public TenantMembershipDeactivatedEvent {
        roleIds = Set.copyOf(roleIds);
    }
}
