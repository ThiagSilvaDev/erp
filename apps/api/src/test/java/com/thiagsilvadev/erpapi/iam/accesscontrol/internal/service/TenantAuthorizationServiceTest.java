package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationScopeType.TENANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantMembership;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRoleAssignment;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;

class TenantAuthorizationServiceTest {

    private final TenantMembershipRepository membershipRepository = mock(TenantMembershipRepository.class);
    private final TenantRoleAssignmentRepository assignmentRepository = mock(TenantRoleAssignmentRepository.class);
    private final TenantRoleRepository roleRepository = mock(TenantRoleRepository.class);
    private final RolePermissionRepository permissionRepository = mock(RolePermissionRepository.class);
    private final TenantAuthorizationService service = new TenantAuthorizationService(
            membershipRepository, assignmentRepository, roleRepository, permissionRepository);

    @Test
    void shouldUnionPermissionsFromMultipleTenantRoles() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(userId, companyId, 4);
        TenantRole sales = role(companyId, "SALES");
        TenantRole inventory = role(companyId, "INVENTORY");
        List<TenantRoleAssignment> assignments = List.of(
                TenantRoleAssignment.tenant(membership.getId(), sales.getId(), companyId),
                TenantRoleAssignment.tenant(membership.getId(), inventory.getId(), companyId));

        when(membershipRepository.findByIdAndUserIdAndCompanyId(membership.getId(), userId, companyId))
                .thenReturn(Optional.of(membership));
        when(assignmentRepository.findByMembershipIdAndScopeTypeAndScopeId(membership.getId(), TENANT, companyId))
                .thenReturn(assignments);
        when(roleRepository.findAllById(Set.of(sales.getId(), inventory.getId())))
                .thenReturn(List.of(sales, inventory));
        when(permissionRepository.findByRoleIdIn(Set.of(sales.getId(), inventory.getId())))
                .thenReturn(List.of(
                        RolePermission.create(sales.getId(), "sales:read"),
                        RolePermission.create(inventory.getId(), "inventory:read"),
                        RolePermission.create(inventory.getId(), "sales:read")));

        TenantAuthorizationSnapshot snapshot = service.validateScopedToken(membership.getId(), userId, companyId, 4);

        assertThat(snapshot.roles())
                .extracting(TenantAuthorizationSnapshot.RoleSummary::code)
                .containsExactlyInAnyOrder("SALES", "INVENTORY");
        assertThat(snapshot.permissions()).containsExactlyInAnyOrder("sales:read", "inventory:read");
    }

    @Test
    void shouldRejectTokenAfterAuthorizationVersionChanges() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        TenantMembership membership = membership(userId, companyId, 5);
        when(membershipRepository.findByIdAndUserIdAndCompanyId(membership.getId(), userId, companyId))
                .thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> service.validateScopedToken(membership.getId(), userId, companyId, 4))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("authorization has changed");
    }

    private static TenantMembership membership(UUID userId, UUID companyId, int authorizationVersion) {
        TenantMembership membership = TenantMembership.create(userId, companyId);
        ReflectionTestUtils.setField(membership, "id", UUID.randomUUID());
        for (int index = 0; index < authorizationVersion; index++) {
            membership.touchAuthorization();
        }
        return membership;
    }

    private static TenantRole role(UUID companyId, String code) {
        TenantRole role = TenantRole.builtIn(companyId, code, code, null);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }
}
