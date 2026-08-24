package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record TenantAuthorizationSnapshot(
        UUID membershipId,
        UUID userId,
        UUID companyId,
        AuthorizationStatus status,
        long authorizationVersion,
        List<RoleSummary> roles,
        Set<String> permissions) {

    public record RoleSummary(UUID id, String code, String name) {}
}
