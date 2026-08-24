package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationScopeType.TENANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantMembership;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleAssignment;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;

class TenantUserAccessFacadeImplTest {

    private final TenantMembershipRepository membershipRepository = mock(TenantMembershipRepository.class);
    private final TenantRoleRepository roleRepository = mock(TenantRoleRepository.class);
    private final TenantRoleAssignmentRepository assignmentRepository = mock(TenantRoleAssignmentRepository.class);
    private final TenantUserAccessFacadeImpl facade =
            new TenantUserAccessFacadeImpl(membershipRepository, roleRepository, assignmentRepository);

    @Test
    void shouldReturnOnlyActiveUserIds() {
        UUID companyId = UUID.randomUUID();
        TenantMembership active = membership(companyId, true);
        when(membershipRepository.findByCompanyIdAndStatus(companyId, ACTIVE)).thenReturn(List.of(active));

        Set<UUID> userIds = facade.getActiveUserIds(companyId);

        assertThat(userIds).containsExactly(active.getUserId());
        verify(membershipRepository).findByCompanyIdAndStatus(companyId, ACTIVE);
    }

    @Test
    void shouldReportActiveMembershipOnlyWhenActive() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        TenantMembership active = membership(companyId, true);
        TenantMembership inactive = membership(companyId, false);
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId))
                .thenReturn(Optional.of(active), Optional.of(inactive), Optional.empty());

        assertThat(facade.hasActiveMembership(userId, companyId)).isTrue();
        assertThat(facade.hasActiveMembership(userId, companyId)).isFalse();
        assertThat(facade.hasActiveMembership(userId, companyId)).isFalse();
    }

    @Test
    void shouldReportRoleWhenAssignedToActiveMembership() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(companyId, true);
        TenantRole role = role(companyId, "MANAGER");
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.of(membership));
        when(roleRepository.findByCompanyIdAndCode(companyId, "MANAGER")).thenReturn(Optional.of(role));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(List.of(TenantRoleAssignment.tenant(membership.getId(), role.getId(), companyId)));

        assertThat(facade.hasRole(userId, companyId, "MANAGER")).isTrue();
    }

    @Test
    void shouldRejectRoleForInactiveMembership() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(companyId, false);
        TenantRole role = role(companyId, "MANAGER");
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.of(membership));
        when(roleRepository.findByCompanyIdAndCode(companyId, "MANAGER")).thenReturn(Optional.of(role));

        assertThat(facade.hasRole(userId, companyId, "MANAGER")).isFalse();
    }

    @Test
    void shouldRejectUnknownRoleCode() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(membershipRepository.findByUserIdAndCompanyId(userId, companyId))
                .thenReturn(Optional.of(membership(companyId, true)));
        when(roleRepository.findByCompanyIdAndCode(companyId, "MANAGER")).thenReturn(Optional.empty());

        assertThat(facade.hasRole(userId, companyId, "MANAGER")).isFalse();
    }

    private static TenantMembership membership(UUID companyId, boolean active) {
        UUID userId = UUID.randomUUID();
        TenantMembership membership = TenantMembership.create(userId, companyId);
        ReflectionTestUtils.setField(membership, "id", UUID.randomUUID());
        if (!active) {
            membership.deactivate();
        }
        return membership;
    }

    private static TenantRole role(UUID companyId, String code) {
        TenantRole role = TenantRole.builtIn(companyId, code, code, null);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }
}
