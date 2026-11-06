package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions.MEMBERSHIP_READ;
import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions.MEMBERSHIP_WRITE;
import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions.ROLE_ASSIGN;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.CreateTenantMembershipRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.ReplaceMembershipRolesRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantMembershipResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper.TenantMembershipMapper;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.TenantAuthorizationService;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.TenantMembershipService;

@Component
public class TenantMembershipUseCase {

    private final TenantMembershipService membershipService;
    private final TenantAuthorizationService authorizationService;
    private final TenantMembershipMapper membershipMapper;

    public TenantMembershipUseCase(
            TenantMembershipService membershipService,
            TenantAuthorizationService authorizationService,
            TenantMembershipMapper membershipMapper) {
        this.membershipService = membershipService;
        this.authorizationService = authorizationService;
        this.membershipMapper = membershipMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + MEMBERSHIP_READ + "')")
    public List<TenantMembershipResponse> listMemberships() {
        return authorizationService.getMemberships(TenantContext.requireTenantId()).stream()
                .map(membershipMapper::toResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + MEMBERSHIP_WRITE + "') and hasAuthority('" + ROLE_ASSIGN + "')")
    public TenantMembershipResponse createMembership(CreateTenantMembershipRequest request) {
        UUID companyId = TenantContext.requireTenantId();
        membershipService.provision(request.userId(), companyId, request.roleIds());
        return membershipMapper.toResponse(authorizationService.getActiveMembership(request.userId(), companyId));
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + MEMBERSHIP_WRITE + "') and hasAuthority('" + ROLE_ASSIGN + "')")
    public TenantMembershipResponse replaceMembershipRoles(UUID membershipId, ReplaceMembershipRolesRequest request) {
        UUID companyId = TenantContext.requireTenantId();
        membershipService.replaceRoles(membershipId, companyId, request.roleIds());
        return membershipMapper.toResponse(authorizationService.getMembership(membershipId, companyId));
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + MEMBERSHIP_WRITE + "')")
    public TenantMembershipResponse activateMembership(UUID membershipId) {
        UUID companyId = TenantContext.requireTenantId();
        membershipService.activate(membershipId, companyId);
        return membershipMapper.toResponse(authorizationService.getMembership(membershipId, companyId));
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + MEMBERSHIP_WRITE + "')")
    public TenantMembershipResponse deactivateMembership(UUID membershipId) {
        UUID companyId = TenantContext.requireTenantId();
        membershipService.deactivate(membershipId, companyId);
        return membershipMapper.toResponse(authorizationService.getMembership(membershipId, companyId));
    }
}
