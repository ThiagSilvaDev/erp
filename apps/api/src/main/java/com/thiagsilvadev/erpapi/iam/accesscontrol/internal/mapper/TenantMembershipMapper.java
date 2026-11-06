package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantMembershipResponse;

@Component
public class TenantMembershipMapper {

    public TenantMembershipResponse toResponse(TenantAuthorizationSnapshot snapshot) {
        return new TenantMembershipResponse(
                snapshot.membershipId(),
                snapshot.userId(),
                snapshot.companyId(),
                snapshot.status(),
                snapshot.authorizationVersion(),
                snapshot.roles().stream()
                        .map(role -> new TenantMembershipResponse.RoleResponse(role.id(), role.code(), role.name()))
                        .toList(),
                snapshot.permissions());
    }
}
