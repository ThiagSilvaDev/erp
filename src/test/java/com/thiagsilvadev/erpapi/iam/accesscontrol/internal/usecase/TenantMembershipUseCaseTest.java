package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.CreateTenantMembershipRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.ReplaceMembershipRolesRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper.TenantMembershipMapper;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.TenantAuthorizationService;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.TenantMembershipService;

class TenantMembershipUseCaseTest {

    private final TenantMembershipService membershipService = mock(TenantMembershipService.class);
    private final TenantAuthorizationService authorizationService = mock(TenantAuthorizationService.class);
    private final TenantMembershipUseCase useCase =
            new TenantMembershipUseCase(membershipService, authorizationService, new TenantMembershipMapper());

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldListMemberships() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        TenantAuthorizationSnapshot snapshot = snapshot(companyId);
        when(authorizationService.getMemberships(companyId)).thenReturn(List.of(snapshot));

        var responses = useCase.listMemberships();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(snapshot.membershipId());
        assertThat(responses.getFirst().companyId()).isEqualTo(companyId);
        assertThat(responses.getFirst().roles()).extracting(role -> role.code()).containsExactly("MANAGER");
    }

    @Test
    void shouldCreateMembership() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        TenantAuthorizationSnapshot snapshot = snapshot(companyId);
        when(authorizationService.getActiveMembership(userId, companyId)).thenReturn(snapshot);

        var response = useCase.createMembership(new CreateTenantMembershipRequest(userId, Set.of(roleId)));

        verify(membershipService).provision(userId, companyId, Set.of(roleId));
        assertThat(response.id()).isEqualTo(snapshot.membershipId());
    }

    @Test
    void shouldReplaceMembershipRoles() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        UUID membershipId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        TenantAuthorizationSnapshot snapshot = snapshot(companyId);
        when(authorizationService.getMembership(membershipId, companyId)).thenReturn(snapshot);

        var response = useCase.replaceMembershipRoles(membershipId, new ReplaceMembershipRolesRequest(Set.of(roleId)));

        verify(membershipService).replaceRoles(membershipId, companyId, Set.of(roleId));
        assertThat(response.id()).isEqualTo(snapshot.membershipId());
    }

    @Test
    void shouldActivateMembership() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        UUID membershipId = UUID.randomUUID();
        TenantAuthorizationSnapshot snapshot = snapshot(companyId);
        when(authorizationService.getMembership(membershipId, companyId)).thenReturn(snapshot);

        var response = useCase.activateMembership(membershipId);

        verify(membershipService).activate(membershipId, companyId);
        assertThat(response.id()).isEqualTo(snapshot.membershipId());
    }

    @Test
    void shouldDeactivateMembership() {
        UUID companyId = UUID.randomUUID();
        TenantContext.setTenantId(companyId);
        UUID membershipId = UUID.randomUUID();
        TenantAuthorizationSnapshot snapshot = snapshot(companyId);
        when(authorizationService.getMembership(membershipId, companyId)).thenReturn(snapshot);

        var response = useCase.deactivateMembership(membershipId);

        verify(membershipService).deactivate(membershipId, companyId);
        assertThat(response.id()).isEqualTo(snapshot.membershipId());
    }

    private static TenantAuthorizationSnapshot snapshot(UUID companyId) {
        return new TenantAuthorizationSnapshot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                companyId,
                ACTIVE,
                1L,
                List.of(new TenantAuthorizationSnapshot.RoleSummary(UUID.randomUUID(), "MANAGER", "Manager")),
                Set.of("catalog:read"));
    }
}
