package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;

public record TenantMembershipResponse(
        UUID id,
        UUID userId,
        UUID companyId,
        AuthorizationStatus status,
        long authorizationVersion,
        List<RoleResponse> roles,
        Set<String> effectivePermissions) {

    public record RoleResponse(UUID id, String code, String name) {}
}
