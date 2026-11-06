package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;

class TenantMembershipMapperTest {

    private final TenantMembershipMapper mapper = new TenantMembershipMapper();

    @Test
    void shouldMapSnapshotToResponse() {
        UUID membershipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        var snapshot = new TenantAuthorizationSnapshot(
                membershipId,
                userId,
                companyId,
                ACTIVE,
                3L,
                List.of(new TenantAuthorizationSnapshot.RoleSummary(UUID.randomUUID(), "MANAGER", "Manager")),
                Set.of("catalog:read"));

        var response = mapper.toResponse(snapshot);

        assertThat(response.id()).isEqualTo(membershipId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.companyId()).isEqualTo(companyId);
        assertThat(response.status()).isEqualTo(ACTIVE);
        assertThat(response.authorizationVersion()).isEqualTo(3L);
        assertThat(response.roles()).hasSize(1);
        assertThat(response.roles().getFirst().code()).isEqualTo("MANAGER");
        assertThat(response.roles().getFirst().name()).isEqualTo("Manager");
        assertThat(response.effectivePermissions()).isEqualTo(Set.of("catalog:read"));
    }
}
